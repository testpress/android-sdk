package in.testpress.v2_4.models;

import java.util.ArrayList;
import java.util.List;

import in.testpress.models.greendao.Attachment;
import in.testpress.models.greendao.Bookmark;
import in.testpress.models.greendao.BookmarkFolder;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentType;
import in.testpress.models.greendao.Direction;
import in.testpress.models.greendao.DirectionTranslation;
import in.testpress.models.greendao.HtmlContent;
import in.testpress.models.greendao.ReviewAnswer;
import in.testpress.models.greendao.ReviewAnswerTranslation;
import in.testpress.models.greendao.ReviewItem;
import in.testpress.models.greendao.ReviewQuestion;
import in.testpress.models.greendao.ReviewQuestionTranslation;
import in.testpress.models.greendao.Subject;
import in.testpress.models.greendao.Video;

public class BookmarksListResponse {

    private List<Bookmark> bookmarks = new ArrayList<>();
    private List<BookmarkFolder> folders = new ArrayList<>();
    private List<ContentType> contentTypes = new ArrayList<>();

    private List<ReviewItem> userSelectedAnswers = new ArrayList<>();
    private List<ReviewQuestion> questions = new ArrayList<>();
    private List<ReviewAnswer> answers = new ArrayList<>();
    private List<ReviewQuestionTranslation> translations = new ArrayList<>();
    private List<ReviewAnswerTranslation> answerTranslations = new ArrayList<>();

    private List<Direction> directions = new ArrayList<>();
    private List<DirectionTranslation> directionTranslations = new ArrayList<>();
    private List<Subject> subjects = new ArrayList<>();

    private List<Content> chapterContents = new ArrayList<>();
    private List<HtmlContent> htmlContents = new ArrayList<>();
    private List<Video> videos = new ArrayList<>();
    private List<Attachment> attachments = new ArrayList<>();

    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(List<Bookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public List<BookmarkFolder> getFolders() {
        return folders;
    }

    public void setFolders(List<BookmarkFolder> folders) {
        this.folders = folders;
    }

    public List<ContentType> getContentTypes() {
        return contentTypes;
    }

    public void setContentTypes(List<ContentType> contentTypes) {
        this.contentTypes = contentTypes;
    }

    public List<ReviewItem> getUserSelectedAnswers() {
        return userSelectedAnswers;
    }

    public void setUserSelectedAnswers(List<ReviewItem> userSelectedAnswers) {
        this.userSelectedAnswers = userSelectedAnswers;
    }

    public List<ReviewQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ReviewQuestion> questions) {
        this.questions = questions;
    }

    public List<ReviewAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ReviewAnswer> answers) {
        this.answers = answers;
    }

    public List<ReviewQuestionTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<ReviewQuestionTranslation> translations) {
        this.translations = translations;
    }

    public List<ReviewAnswerTranslation> getAnswerTranslations() {
        return answerTranslations;
    }

    public void setAnswerTranslations(List<ReviewAnswerTranslation> answerTranslations) {
        this.answerTranslations = answerTranslations;
    }

    public List<Direction> getDirections() {
        return directions;
    }

    public void setDirections(List<Direction> directions) {
        this.directions = directions;
    }

    public List<DirectionTranslation> getDirectionTranslations() {
        return directionTranslations;
    }

    public void setDirectionTranslations(List<DirectionTranslation> directionTranslations) {
        this.directionTranslations = directionTranslations;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public List<Content> getChapterContents() {
        return chapterContents;
    }

    public void setChapterContents(List<Content> chapterContents) {
        this.chapterContents = chapterContents;
    }

    public List<HtmlContent> getHtmlContents() {
        return htmlContents;
    }

    public void setHtmlContents(List<HtmlContent> htmlContents) {
        this.htmlContents = htmlContents;
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

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}