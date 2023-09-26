package in.testpress.v2_4.models;

import java.util.ArrayList;
import java.util.List;

import in.testpress.models.greendao.Attachment;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.HtmlContent;
import in.testpress.models.greendao.LiveStream;
import in.testpress.models.greendao.Price;
import in.testpress.models.greendao.Product;
import in.testpress.models.greendao.Stream;
import in.testpress.models.greendao.Video;
import in.testpress.models.greendao.VideoConference;

public class ContentsListResponse {
    private List<Exam> exams = new ArrayList<>();
    private List<Content> contents = new ArrayList<>();
    private List<Video> videos = new ArrayList<>();
    private List<Attachment> attachments = new ArrayList<>();
    private List<HtmlContent> notes = new ArrayList<>();
    private List<Stream> streams = new ArrayList<>();
    private List<VideoConference> videoConferences = new ArrayList<>();
    private List<LiveStream> liveStreams = new ArrayList<>();


    public List<Exam> getExams() {
        return exams;
    }

    public void setExams(List<Exam> exams) {
        this.exams = exams;
    }

    public List<Content> getContents() {
        return contents;
    }

    public void setContents(
            List<Content> contents) {
        this.contents = contents;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(
            List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public List<HtmlContent> getNotes() {
        return notes;
    }

    public void setNotes(List<HtmlContent> notes) {
        this.notes = notes;
    }

    public List<Stream> getStreams() {
        return streams;
    }

    public void setStreams(List<Stream> streams) {
        this.streams = streams;
    }

    public List<VideoConference> getVideoConferences() {
        return videoConferences;
    }

    public List<LiveStream> getLiveStreams() {
        return this.liveStreams;
    }

    public void setVideoConferences(List<VideoConference> videoConferences) {
        this.videoConferences = videoConferences;
    }
}
