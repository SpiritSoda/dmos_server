package com.dmos.dmos_server.channel;

import com.dmos.dmos_common.channel.ChannelState;
import io.netty.channel.Channel;
import lombok.Data;

@Data
public class ChannelHandle {
    // 客户端id
    private int id;
    // 连接通道
    private Channel channel;
    // 状态 见dmos_common.channel.ChannelState
    private int state;
    private boolean heartbeat;
    public ChannelHandle(Channel channel){
        this.channel = channel;
        this.id = 0;
        this.state = ChannelState.CONNECTED;
        this.heartbeat = true;
    }
    public boolean established(){
        return this.state == ChannelState.ESTABLISHED;
    }
    public void establish(int client){
        this.state = ChannelState.ESTABLISHED;
        this.id = client;
    }
    public void heartbeat(boolean v){
        this.heartbeat = v;
    }
}
