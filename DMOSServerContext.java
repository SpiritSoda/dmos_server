package com.dmos.dmos_server;

import com.dmos.dmos_server.channel.ChannelHandle;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DMOSServerContext {
    private static Map<String, ChannelHandle> channels = new ConcurrentHashMap<>();
    private static Map<Integer, String> clients = new ConcurrentHashMap<>();

    // 基本channel操作
    public void saveChannel(Channel channel){
        channels.put(channel.id().asLongText(), new ChannelHandle(channel));
    }
    public void verifyChannel(String channel, int client){
        clients.put(client, channel);
        channels.get(channel).establish(client);
    }

    public Channel getChannel(int client){
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
        Channel channel = getChannel(id);
        channel.writeAndFlush(o);
    }
}
