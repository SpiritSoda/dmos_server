package com.dmos.dmos_server.tree;

import lombok.Data;

import java.util.List;

@Data
public class ReportChangeLog {
    private List<Integer> online;
    private List<Integer> offline;
    public ReportChangeLog(List<Integer> online, List<Integer> offline){
        this.offline = offline;
        this.online = online;
    }
}
