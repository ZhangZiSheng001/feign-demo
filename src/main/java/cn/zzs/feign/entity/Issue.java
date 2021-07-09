package cn.zzs.feign.entity;

import java.util.List;

public class Issue {
    
    String title;
    String body;
    List<String> assignees;
    int milestone;
    List<String> labels;
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public List<String> getAssignees() {
        return assignees;
    }
    
    public void setAssignees(List<String> assignees) {
        this.assignees = assignees;
    }
    
    public int getMilestone() {
        return milestone;
    }
    
    public void setMilestone(int milestone) {
        this.milestone = milestone;
    }
    
    public List<String> getLabels() {
        return labels;
    }
    
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
}
