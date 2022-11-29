package com.dmos.dmos_server;

import com.dmos.dmos_common.data.ServerReportDTO;
import com.dmos.dmos_common.util.ParseUtil;
import com.dmos.dmos_server.tree.TreeNode;
import com.dmos.dmos_server.channel.ChannelHandle;
import com.dmos.dmos_server.tree.ReportChangeLog;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DMOSServerContext {
    // 当前节点通道
    private static Map<String, ChannelHandle> channels = new ConcurrentHashMap<>();
    // 节点-通道映射
    private static Map<Integer, String> clients = new ConcurrentHashMap<>();
    // 子树的节点关系
    private Map<Integer, Integer> nodes = new ConcurrentHashMap<>();

    // ====================== channel相关操作 ============================ //
    // 基本channel操作
    public void saveChannel(Channel channel){
        channels.put(channel.id().asLongText(), new ChannelHandle(channel));
    }
    public void verifyChannel(String channel, int client){
        clients.put(client, channel);
        channels.get(channel).establish(client);
    }

    public Channel getChannel(int client){
        if(!clients.containsKey(client))
            return null;
        return channels.get(clients.get(client)).getChannel();
    }
    public ChannelHandle getChannelHandle(int client){
        return channels.get(clients.get(client));
    }

    public ChannelHandle getChannelHandle(String channel){
        return channels.get(channel);
    }

    public void deleteChannel(String channel){
        ChannelHandle channelHandle = channels.remove(channel);
        if(channelHandle == null)
            return;
        if(channelHandle.established())
            clients.remove(channelHandle.getId());
    }
    public void deleteChannel(int client){
        if(!clients.containsKey(client))
            return;
        String channel = clients.remove(client);
        channels.remove(channel);
    }

    public int size(){
        return clients.size();
    }

    public Set<Integer> getClients(){
        return clients.keySet();
    }

    // 心跳管理
    public void heartbeat(String channel){
        if(channels.get(channel) != null)
            channels.get(channel).heartbeat(true);
    }
    public void resetHeartbeat(){
        for(String channel: channels.keySet()){
            channels.get(channel).heartbeat(false);
        }
    }
    public boolean established(String channel){
        return channels.get(channel).established();
    }
    public void disconnectTimeout() throws InterruptedException {
        Set<String> timeout = new HashSet<>();
        for(String channel: channels.keySet()){
            if(!channels.get(channel).isHeartbeat())
                timeout.add(channel);
        }
        for(String channel: timeout){
            ChannelHandle handle = channels.remove(channel);
            log.info("客户机 {} 超时断开", handle.getId());
            clients.remove(handle.getId());
            handle.getChannel().close().sync();
        }
    }
    // 消息发送
    public void sendTo(int id, Object o){
        int route = findRoute(id);
        Channel channel = getChannel(route);
        if(channel != null && channel.isActive()){
            channel.write(ParseUtil.encode(o, false) + "\r");
            channel.flush();
        }
    }
    public void sendToWithoutFlush(int id, Object o){
        int route = findRoute(id);
        Channel channel = getChannel(route);
        if(channel != null && channel.isActive()){
            channel.write(ParseUtil.encode(o, false) + "\r");
        }
    }
    // ====================== 节点相关操作 ============================ //

    public HashSet<Integer> getChild(int id){
        HashSet<Integer> child = new HashSet<>();
        for(Integer node: nodes.keySet()){
            if(nodes.get(node) == id)
                child.add(node);
        }
        return child;
    }
    public TreeNode getTree(){
        TreeNode root = new TreeNode(0);
        root.setChildren(getTreeChild(0));
        return root;
    }
    private List<TreeNode> getTreeChild(int id){
        List<TreeNode> nodes = new ArrayList<>();
        HashSet<Integer> childs = getChild(id);
        for(Integer child: childs){
            TreeNode node = new TreeNode(child);
            node.setChildren(getTreeChild(child));
            nodes.add(node);
        }
        return nodes;
    }
    // 获取前往节点id的路线
    public int findRoute(int id){
        int route = id;
        while(nodes.containsKey(route) && !clients.containsKey(route)){
            route = nodes.get(route);
        }
        if(!clients.containsKey(route)){
            nodes.remove(id);
            return -1;
        }
        return route;
    }
    public ReportChangeLog report(ServerReportDTO reportDTO){
        int id = reportDTO.getId();
        HashSet<Integer> child = getChild(id);
        List<Integer> online = new ArrayList<>(), offline = new ArrayList<>();
        for(Integer node: reportDTO.getChild()){
            if(!child.contains(node)){
                online.add(node);
                nodes.put(node, id);
            }
            else
                child.remove(node);
        }
        for(Integer node: child){
            offline.add(node);
            nodes.remove(node);
        }
        return new ReportChangeLog(online, offline, reportDTO.getTimestamp());
    }
}

