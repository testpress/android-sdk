package in.testpress;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Index;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.Schema;
import org.greenrobot.greendao.generator.ToMany;
import org.greenrobot.greendao.generator.ToOne;

public class TestpressSDKDaoGenerator {
    // Increase the version if any modification has been made in this file.
    private static final int VERSION = 20;

    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(VERSION, "in.testpress.models.greendao");
        Entity reviewAttempt = addReviewAttempt(schema);
        Entity reviewItem = addReviewItem(schema);
        Entity reviewQuestion = addReviewQuestion(schema);
        Entity reviewAnswer = addReviewAnswer(schema);
        Entity reviewQuestionTranslation = addReviewQuestionTranslation(schema);
        Entity reviewAnswerTranslation = addReviewAnswerTranslation(schema);
        addSelectedAnswer(schema);
        addAttemptToReviewItem(reviewAttempt, reviewItem);
        addReviewItemToQuestion(reviewItem, reviewQuestion);
        addReviewQuestionToAnswers(reviewQuestion, reviewAnswer);
        addTranslationsToReviewQuestion(reviewQuestion, reviewQuestionTranslation);
        addAnswersToReviewTranslations(reviewQuestionTranslation, reviewAnswerTranslation);
        addCourse(schema);
        addChapter(schema);

        Entity html = addHtmlContent(schema);
        Entity video = addVideo(schema);
        Entity attachment = addAttachment(schema);
        Entity exam = addExam(schema);
        addLanguage(schema, exam);
        Entity content = addContent(schema);
        addHTMLToContent(content, html);
        addVideoToContent(content, video);
        addAttachmentToContent(content, attachment);
        addExamToContent(content, exam);


        Entity courseAttempt = addCourseAttempt(schema);
        Entity attempt = addAttempt(schema);
        addCourseContentToCourseAttempt(courseAttempt, content);
        addAttemptToCourseAttempt(courseAttempt, attempt);
        Entity videoAttempt = addVideoAttempt(schema);
        addVideoAttemptToCourseAttempt(courseAttempt, videoAttempt);
        addVideoToVideoAttempt(videoAttempt, video);
        Entity attemptSection = addAttemptSection(schema);
        addSectionToAttempt(attempt, attemptSection);

        addBookmarkFolder(schema);
        Entity bookmark = addBookmark(schema);
        Entity contentType = addContentType(schema);
        addContentTypeToBookmark(bookmark, contentType);

        Entity answerTranslation = addAnswerTranslation(schema);
        addAnswersTranslationToReviewTranslations(reviewQuestionTranslation, answerTranslation);
        addSubject(schema);
        addDirection(schema);
        addDirectionTranslation(schema);

        schema.enableKeepSectionsByDefault();
        new DaoGenerator().generateAll(schema, "core/src/main/java");
    }

    private static Entity addAttempt(Schema schema) {
        Entity attempt = schema.addEntity("Attempt");
        attempt.addStringProperty("url");
        attempt.addLongProperty("id").primaryKey();
        attempt.addStringProperty("date");
        attempt.addIntProperty("totalQuestions");
        attempt.addStringProperty("score");
        attempt.addStringProperty("rank");
        attempt.addStringProperty("maxRank");
        attempt.addStringProperty("reviewUrl");
        attempt.addStringProperty("questionsUrl");
        attempt.addIntProperty("correctCount");
        attempt.addIntProperty("incorrectCount");
        attempt.addStringProperty("lastStartedTime");
        attempt.addStringProperty("remainingTime");
        attempt.addStringProperty("timeTaken");
        attempt.addStringProperty("state");
        attempt.addStringProperty("percentile");
        attempt.addIntProperty("speed");
        attempt.addIntProperty("accuracy");
        attempt.addStringProperty("percentage");
        attempt.implementsInterface("android.os.Parcelable");
        return attempt;

    }

    private static Entity addCourseAttempt(Schema schema) {
        Entity courseAttempt = schema.addEntity("CourseAttempt");
        courseAttempt.addLongProperty("id").primaryKey();
        courseAttempt.addStringProperty("type");
        courseAttempt.addIntProperty("objectId");
        courseAttempt.addStringProperty("objectUrl");
        courseAttempt.addStringProperty("trophies");
        courseAttempt.implementsInterface("android.os.Parcelable");
        return courseAttempt;
    }

    private static void addCourseContentToCourseAttempt(Entity courseAttempt, Entity content) {
        Property courseContentId = courseAttempt.addLongProperty("courseContentId").getProperty();
        courseAttempt.addToOne(content, courseContentId, "chapterContent");
    }

    private static void addAttemptToCourseAttempt(Entity courseAttempt, Entity attempt) {
        Property attemptId = courseAttempt.addLongProperty("attemptId").getProperty();
        courseAttempt.addToOne(attempt, attemptId, "assessment");
    }

    private static void addVideoAttemptToCourseAttempt(Entity courseAttempt, Entity videoAttempt) {
        Property videoAttemptId = courseAttempt.addLongProperty("videoAttemptId").getProperty();
        courseAttempt.addToOne(videoAttempt, videoAttemptId, "video");
    }

    private static void addVideoToVideoAttempt(Entity videoAttempt, Entity videoContent) {
        Property videoContentId = videoAttempt.addLongProperty("videoContentId").getProperty();
        videoAttempt.addToOne(videoContent, videoContentId, "videoContent");
    }

    private static void addExamToContent(Entity content, Entity exam) {
        Property examId = content.addLongProperty("examId").getProperty();
        content.addToOne(exam, examId, "exam");
    }

    private static void addHTMLToContent(Entity content, Entity html) {
        Property htmlId = content.addLongProperty("htmlId").getProperty();
        content.addToOne(html, htmlId, "html");
    }

    private static void addVideoToContent(Entity content, Entity video) {
        Property videoId = content.addLongProperty("videoId").getProperty();
        content.addToOne(video, videoId, "video");
    }

    private static void addAttachmentToContent(Entity content, Entity attachment) {
        Property attachmentId = content.addLongProperty("attachmentId").getProperty();
        content.addToOne(attachment, attachmentId, "attachment");
    }

    private static Entity addHtmlContent(Schema schema) {
        Entity htmlContent = schema.addEntity("HtmlContent");
        htmlContent.addLongProperty("id").primaryKey();
        htmlContent.addStringProperty("title");
        htmlContent.addStringProperty("textHtml");
        htmlContent.addStringProperty("sourceUrl");
        return htmlContent;
    }

    private static Entity addContent(Schema schema) {
        Entity content = schema.addEntity("Content");
        content.addIntProperty("order");
        content.addStringProperty("htmlContentTitle");
        content.addStringProperty("htmlContentUrl");
        content.addStringProperty("url");
        content.addStringProperty("attemptsUrl");
        content.addIntProperty("chapterId");
        content.addStringProperty("chapterSlug");
        content.addStringProperty("chapterUrl");
        content.addLongProperty("id").primaryKey();
        content.addStringProperty("name");
        content.addStringProperty("image");
        content.addStringProperty("description");
        content.addBooleanProperty("isLocked");
        content.addIntProperty("attemptsCount");
        content.addStringProperty("start");
        content.addStringProperty("end");
        content.addBooleanProperty("hasStarted");
        content.addBooleanProperty("active");
        content.addLongProperty("bookmarkId");
        content.addIntProperty("videoWatchedPercentage").notNull();
        content.implementsInterface("android.os.Parcelable");
        return content;
    }

    private static Entity addAttachment(Schema schema) {
        Entity attachment = schema.addEntity("Attachment");
        attachment.addStringProperty("title");
        attachment.addStringProperty("attachmentUrl");
        attachment.addStringProperty("description");
        attachment.addLongProperty("id").primaryKey();
        attachment.implementsInterface("android.os.Parcelable");
        return attachment;
    }

    private static Entity addVideo(Schema schema) {
        Entity video = schema.addEntity("Video");
        video.addStringProperty("title");
        video.addStringProperty("url");
        video.addLongProperty("id").primaryKey();
        video.addStringProperty("embedCode");
        video.addLongProperty("duration");
        video.addBooleanProperty("isDomainRestricted");
        video.implementsInterface("android.os.Parcelable");
        return video;
    }

    private static Entity addVideoAttempt(Schema schema) {
        Entity videoAttempt = schema.addEntity("VideoAttempt");
        videoAttempt.addLongProperty("id").primaryKey();
        videoAttempt.addStringProperty("lastPosition");
        videoAttempt.addIntProperty("state");
        videoAttempt.addStringProperty("watchedDuration");
        return videoAttempt;
    }

    private static Entity addLanguage(Schema schema, Entity exam) {
        Entity language = schema.addEntity("Language");
        language.addLongProperty("id").primaryKey().autoincrement();
        Property code = language.addStringProperty("code").getProperty();
        language.addStringProperty("title");
        language.implementsInterface("android.os.Parcelable");

        // Add languages to exam
        Property examId = language.addLongProperty("examId").getProperty();
        exam.addToMany(language, examId, "languages");

        // Unique together code & examId
        Index index = new Index();
        index.addProperty(code);
        index.addProperty(examId);
        index.makeUnique();

        language.addIndex(index);
        return language;
    }

    private static Entity addExam(Schema schema) {
        Entity exam = schema.addEntity("Exam");
        exam.addStringProperty("totalMarks");
        exam.addStringProperty("url");
        exam.addLongProperty("id").primaryKey();
        exam.addIntProperty("attemptsCount");
        exam.addIntProperty("pausedAttemptsCount");
        exam.addStringProperty("title");
        exam.addStringProperty("description");
        exam.addStringProperty("startDate");
        exam.addStringProperty("endDate");
        exam.addStringProperty("duration");
        exam.addIntProperty("numberOfQuestions");
        exam.addStringProperty("negativeMarks");
        exam.addStringProperty("markPerQuestion");
        exam.addIntProperty("templateType");
        exam.addBooleanProperty("allowRetake");
        exam.addBooleanProperty("allowPdf");
        exam.addBooleanProperty("showAnswers");
        exam.addIntProperty("maxRetakes");
        exam.addStringProperty("attemptsUrl");
        exam.addStringProperty("deviceAccessControl");
        exam.addIntProperty("commentsCount");
        exam.addStringProperty("slug");
        exam.addStringProperty("selectedLanguage");
        exam.addBooleanProperty("variableMarkPerQuestion");
        exam.addIntProperty("passPercentage");
        exam.addBooleanProperty("enableRanks");
        exam.addBooleanProperty("showScore");
        exam.addBooleanProperty("showPercentile");
        exam.addStringProperty("categories").customType(
                "in.testpress.util.StringList",
                "in.testpress.util.StringListConverter"
        );
        exam.implementsInterface("android.os.Parcelable");
        return exam;
    }

    private static Entity addAttemptSection(Schema schema) {
        Entity attemptSection = schema.addEntity("AttemptSection");
        attemptSection.addLongProperty("id").primaryKey();
        attemptSection.addStringProperty("state");
        attemptSection.addStringProperty("questionsUrl");
        attemptSection.addStringProperty("startUrl");
        attemptSection.addStringProperty("endUrl");
        attemptSection.addStringProperty("remainingTime");
        attemptSection.addStringProperty("name");
        attemptSection.addStringProperty("duration");
        attemptSection.addIntProperty("order");
        attemptSection.implementsInterface("android.os.Parcelable");
        return attemptSection;
    }

    private static void addSectionToAttempt(Entity attempt, Entity section) {
        Property attemptId = section.addLongProperty("attemptId").getProperty();
        ToMany attemptToSections = attempt.addToMany(section, attemptId);
        attemptToSections.setName("sections");
    }

    private static Entity addCourse(Schema schema) {
        Entity course = schema.addEntity("Course");
        course.addLongProperty("id").primaryKey();
        course.addStringProperty("url");
        course.addStringProperty("title");
        course.addStringProperty("description");
        course.addStringProperty("image");
        course.addStringProperty("modified");
        course.addLongProperty("modifiedDate");
        course.addStringProperty("contentsUrl");
        course.addStringProperty("chaptersUrl");
        course.addStringProperty("slug");
        course.addIntProperty("trophiesCount");
        course.addIntProperty("chaptersCount");
        course.addIntProperty("contentsCount");
        course.addIntProperty("order");
        course.addBooleanProperty("active");
        course.addStringProperty("external_content_link");
        course.addStringProperty("external_link_label");
        return course;
    }

    private static Entity addChapter(Schema schema) {
        Entity chapter = schema.addEntity("Chapter");
        chapter.addLongProperty("id").primaryKey();
        chapter.addStringProperty("name");
        chapter.addStringProperty("description");
        chapter.addStringProperty("slug");
        chapter.addStringProperty("image");
        chapter.addStringProperty("modified");
        chapter.addLongProperty("modifiedDate");
        chapter.addIntProperty("courseId");
        chapter.addStringProperty("courseUrl");
        chapter.addStringProperty("contentUrl");
        chapter.addStringProperty("childrenUrl");
        chapter.addIntProperty("parentId");
        chapter.addStringProperty("parentSlug");
        chapter.addStringProperty("parentUrl");
        chapter.addBooleanProperty("leaf");
        chapter.addStringProperty("url");
        chapter.addIntProperty("requiredTrophyCount");
        chapter.addBooleanProperty("isLocked");
        chapter.addIntProperty("order");
        chapter.addIntProperty("contentsCount");
        chapter.addIntProperty("childrenCount");
        chapter.addBooleanProperty("active");
        return chapter;
    }

    private static Entity addReviewAttempt(Schema schema) {
        Entity attempt = schema.addEntity("ReviewAttempt");
        attempt.addLongProperty("id").primaryKey();
        attempt.addIntProperty("totalQuestions");
        attempt.addStringProperty("score");
        attempt.addStringProperty("rank");
        attempt.addStringProperty("reviewUrl");
        attempt.addIntProperty("correctCount");
        attempt.addIntProperty("incorrectCount");
        attempt.addStringProperty("timeTaken");
        attempt.addStringProperty("percentile");
        attempt.addIntProperty("speed");
        attempt.addIntProperty("accuracy");
        return attempt;
    }

    private static Entity addReviewItem(Schema schema) {
        Entity reviewItem = schema.addEntity("ReviewItem");
        reviewItem.addLongProperty("id").primaryKey();
        reviewItem.addIntProperty("index");
        reviewItem.addStringProperty("url");
        reviewItem.addIntProperty("order");
        reviewItem.addStringProperty("duration");
        reviewItem.addStringProperty("bestDuration");
        reviewItem.addStringProperty("averageDuration");
        reviewItem.addStringProperty("essayText");
        reviewItem.addStringProperty("essayTopic");
        reviewItem.addStringProperty("selectedAnswers").customType(
                "in.testpress.util.IntegerList",
                "in.testpress.util.IntegerListConverter"
        ).codeBeforeField("@SerializedName(value=\"selected_answer_ids\", alternate={\"selected_answers\"})");
        reviewItem.addBooleanProperty("review");
        reviewItem.addIntProperty("commentsCount");
        reviewItem.addIntProperty("correctPercentage");
        reviewItem.addLongProperty("bookmarkId");
        reviewItem.addStringProperty("marks");
        reviewItem.addStringProperty("shortText");
        reviewItem.addStringProperty("result");
        return reviewItem;
    }

    private static Entity addSelectedAnswer(Schema schema) {
        Entity selectedAnswer = schema.addEntity("SelectedAnswer");
        selectedAnswer.addIdProperty().autoincrement();
        selectedAnswer.addIntProperty("answerId");
        selectedAnswer.addLongProperty("reviewItemId");
        return selectedAnswer;
    }

    private static Entity addReviewQuestion(Schema schema) {
        Entity reviewQuestion = schema.addEntity("ReviewQuestion");
        reviewQuestion.addLongProperty("id").primaryKey();
        reviewQuestion.addStringProperty("questionHtml");
        reviewQuestion.addStringProperty("direction");
        reviewQuestion.addStringProperty("subject");
        reviewQuestion.addStringProperty("explanationHtml");
        reviewQuestion.addStringProperty("type");
        reviewQuestion.addStringProperty("commentsUrl");
        reviewQuestion.addStringProperty("language");
        reviewQuestion.addFloatProperty("percentageGotCorrect");
        reviewQuestion.addLongProperty("directionId");
        reviewQuestion.addLongProperty("subjectId");
        reviewQuestion.addStringProperty("answerIds").customType(
                "in.testpress.util.IntegerList",
                "in.testpress.util.IntegerListConverter"
        );
        reviewQuestion.addStringProperty("translationIds").customType(
                "in.testpress.util.IntegerList",
                "in.testpress.util.IntegerListConverter"
        );
        return reviewQuestion;
    }

    private static Entity addReviewQuestionTranslation(Schema schema) {
        Entity reviewQuestionTranslation = schema.addEntity("ReviewQuestionTranslation");
        reviewQuestionTranslation.addIdProperty().autoincrement();
        reviewQuestionTranslation.addStringProperty("questionHtml");
        reviewQuestionTranslation.addStringProperty("direction");
        reviewQuestionTranslation.addStringProperty("explanation");
        reviewQuestionTranslation.addStringProperty("language");
        reviewQuestionTranslation.addLongProperty("directionId");
        reviewQuestionTranslation.addStringProperty("answerIds").customType(
                "in.testpress.util.IntegerList",
                "in.testpress.util.IntegerListConverter"
        );
        return reviewQuestionTranslation;
    }

    private static Entity addReviewAnswer(Schema schema) {
        Entity reviewAnswer = schema.addEntity("ReviewAnswer");
        reviewAnswer.addLongProperty("id").primaryKey();
        reviewAnswer.addStringProperty("textHtml");
        reviewAnswer.addBooleanProperty("isCorrect");
        reviewAnswer.addStringProperty("marks");
        return reviewAnswer;
    }

    private static Entity addReviewAnswerTranslation(Schema schema) {
        Entity reviewAnswerTranslation = schema.addEntity("ReviewAnswerTranslation");
        reviewAnswerTranslation.addLongProperty("id");
        reviewAnswerTranslation.addStringProperty("textHtml");
        reviewAnswerTranslation.addBooleanProperty("isCorrect");
        reviewAnswerTranslation.addStringProperty("marks");
        return reviewAnswerTranslation;
    }

    private static void addAttemptToReviewItem(Entity reviewAttempt, Entity reviewItem) {
        Property attemptId = reviewItem.addLongProperty("attemptId").getProperty();
        ToMany attemptToReviewItems = reviewAttempt.addToMany(reviewItem, attemptId);
        attemptToReviewItems.setName("reviewItems");
    }

    private static void addReviewItemToQuestion(Entity reviewItem, Entity reviewQuestion) {
        Property questionId = reviewItem.addLongProperty("questionId").getProperty();
        ToOne reviewItemToQuestion = reviewItem.addToOne(reviewQuestion, questionId);
        reviewItemToQuestion.setName("question");
    }

    private static void addReviewQuestionToAnswers(Entity reviewQuestion, Entity reviewAnswer) {
        Property reviewQuestionId = reviewAnswer.addLongProperty("questionId").getProperty();
        ToMany questionToAnswers = reviewQuestion.addToMany(reviewAnswer, reviewQuestionId);
        questionToAnswers.setName("answers");
    }

    private static void addTranslationsToReviewQuestion(Entity reviewQuestion, Entity translation) {
        Property reviewQuestionId = translation.addLongProperty("questionId").getProperty();
        ToMany questionToTranslations = reviewQuestion.addToMany(translation, reviewQuestionId);
        questionToTranslations.setName("translations");
    }

    private static void addAnswersToReviewTranslations(Entity questionTranslation, Entity answerTranslation) {
        Property reviewQuestionId = answerTranslation.addLongProperty("questionTranslationId").getProperty();
        ToMany questionToAnswers = questionTranslation.addToMany(answerTranslation, reviewQuestionId);
        questionToAnswers.setName("answers");
    }

    private static void addBookmarkFolder(Schema schema) {
        Entity folder = schema.addEntity("BookmarkFolder");
        folder.addLongProperty("id").primaryKey();
        folder.addStringProperty("name");
        folder.addIntProperty("bookmarksCount");
    }

    private static Entity addBookmark(Schema schema) {
        Entity bookmark = schema.addEntity("Bookmark");
        bookmark.addLongProperty("id").primaryKey();
        bookmark.addStringProperty("folder");
        bookmark.addLongProperty("folderId");
        bookmark.addLongProperty("objectId");
        bookmark.addStringProperty("modified");
        bookmark.addLongProperty("modifiedDate");
        bookmark.addStringProperty("created");
        bookmark.addLongProperty("createdDate");
        bookmark.addBooleanProperty("loadedInAllFolder");
        bookmark.addBooleanProperty("loadedInRespectiveFolder");
        bookmark.addBooleanProperty("active");
        return bookmark;
    }

    private static Entity addContentType(Schema schema) {
        Entity contentType = schema.addEntity("ContentType");
        contentType.addLongProperty("id").primaryKey().autoincrement();
        Property model = contentType.addStringProperty("model").getProperty();
        Property appLabel = contentType.addStringProperty("appLabel").getProperty();

        // Unique together model & app_label
        Index index = new Index();
        index.addProperty(model);
        index.addProperty(appLabel);
        index.makeUnique();

        contentType.addIndex(index);
        return contentType;
    }

    private static void addContentTypeToBookmark(Entity bookmark, Entity contentType) {
        Property contentTypeId = bookmark.addLongProperty("contentTypeId").getProperty();
        bookmark.addToOne(contentType, contentTypeId, "contentType");
    }

    private static Entity addAnswerTranslation(Schema schema) {
        Entity answerTranslation = schema.addEntity("AnswerTranslation");
        answerTranslation.addLongProperty("id").primaryKey();
        answerTranslation.addStringProperty("textHtml");
        answerTranslation.addLongProperty("answerId");
        return answerTranslation;
    }

    private static void addAnswersTranslationToReviewTranslations(Entity questionTranslation,
                                                                  Entity answerTranslation) {

        Property questionId = answerTranslation.addLongProperty("questionId").getProperty();
        ToMany questionToAnswers = questionTranslation.addToMany(answerTranslation, questionId);
        questionToAnswers.setName("answerTranslations");
    }

    private static void addSubject(Schema schema) {
        Entity subject = schema.addEntity("Subject");
        subject.addLongProperty("id").primaryKey();
        subject.addStringProperty("name");
    }

    private static void addDirection(Schema schema) {
        Entity direction = schema.addEntity("Direction");
        direction.addLongProperty("id").primaryKey();
        direction.addStringProperty("html");
    }

    private static void addDirectionTranslation(Schema schema) {
        Entity direction = schema.addEntity("DirectionTranslation");
        direction.addLongProperty("id").primaryKey();
        direction.addStringProperty("html");
    }

}
