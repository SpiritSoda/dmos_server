package com.dmos.dmos_server.tree;

import lombok.Data;

import java.util.List;

@Data
public class ReportChangeLog {
    private List<Integer> online;
    private List<Integer> offline;
    private long timestamp;
    public ReportChangeLog(List<Integer> online, List<Integer> offline, long timestamp){
        this.offline = offline;
        this.online = online;
        this.timestamp = timestamp;
    }
    public boolean hasChange(){
        return online.size() > 0 || offline.size() > 0;
    }
}
