package in.testpress;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.Schema;
import org.greenrobot.greendao.generator.ToMany;
import org.greenrobot.greendao.generator.ToOne;

public class TestpressSDKDaoGenerator {
    // Increase the version if any modification has been made in this file.
    private static final int VERSION = 6;

    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(VERSION, "in.testpress.models.greendao");

        Entity attempt = addAttempt(schema);
        Entity reviewItem = addReviewItem(schema);
        Entity reviewQuestion = addReviewQuestion(schema);
        Entity reviewAnswer = addReviewAnswer(schema);
        Entity reviewQuestionTranslation = addReviewQuestionTranslation(schema);
        Entity reviewAnswerTranslation = addReviewAnswerTranslation(schema);
        addSelectedAnswer(schema);
        addAttemptToReviewItem(attempt, reviewItem);
        addReviewItemToQuestion(reviewItem, reviewQuestion);
        addReviewQuestionToAnswers(reviewQuestion, reviewAnswer);
        addTranslationsToReviewQuestion(reviewQuestion, reviewQuestionTranslation);
        addAnswersToReviewTranslations(reviewQuestionTranslation, reviewAnswerTranslation);
        addCourse(schema);
        addChapter(schema);

        Entity language = addLanguage(schema);
        Entity video = addVideo(schema);
        Entity attachment = addAttachment(schema);
        Entity exam = addExam(schema);
        Entity content = addContent(schema);
        //addLanguageToExam(language, exam);
        addVideoToContent(video, content);
        addAttachmentToContent(attachment, content);
        addExamToContent(exam, content);

        new DaoGenerator().generateAll(schema, "../core/src/main/java");
    }

    private static void addLanguageToExam(Entity language, Entity exam) {
        Property examId = language.addLongProperty("examId").getProperty();
        ToMany examToLanguages = exam.addToMany(language, examId, "languages");
    }

    private static void addExamToContent(Entity exam, Entity content) {
        Property examId = content.addLongProperty("examId").getProperty();
        ToOne examToContent = content.addToOne(exam, examId, "exam");
    }

    private static void addVideoToContent(Entity video, Entity content) {
        Property videoId = content.addLongProperty("videoId").getProperty();
        ToOne videoToContent = content.addToOne(video, videoId, "video");
    }

    private static void addAttachmentToContent(Entity attachment, Entity content) {
        Property attachmentId = content.addLongProperty("attachmentId").getProperty();
        ToOne attachmentToContent = content.addToOne(attachment, attachmentId, "attachment");
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
        return content;
    }

    private static Entity addAttachment(Schema schema) {
        Entity attachment = schema.addEntity("Attachment");
        attachment.addStringProperty("title");
        attachment.addStringProperty("attachmentUrl");
        attachment.addStringProperty("description");
        attachment.addLongProperty("id").primaryKey();
        return attachment;
    }

    private static Entity addVideo(Schema schema) {
        Entity video = schema.addEntity("Video");
        video.addStringProperty("title");
        video.addStringProperty("url");
        video.addLongProperty("id").primaryKey();
        video.addStringProperty("embedCode");
        return video;
    }

    private static Entity addLanguage(Schema schema) {
        Entity language = schema.addEntity("Language");
        language.addLongProperty("id").primaryKey().autoincrement();
        language.addStringProperty("code");
        language.addStringProperty("title");
        language.addStringProperty("exam_slug");
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
        exam.addStringProperty("course_category");
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
        return exam;
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
        return chapter;
    }

    private static Entity addAttempt(Schema schema) {
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
        reviewItem.addStringProperty("selectedAnswers").customType(
                "in.testpress.util.IntegerList",
                "in.testpress.util.IntegerListConverter"
        );
        reviewItem.addBooleanProperty("review");
        reviewItem.addIntProperty("commentsCount");
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
        reviewQuestion.addStringProperty("commentsUrl");
        reviewQuestion.addStringProperty("language");
        return reviewQuestion;
    }

    private static Entity addReviewQuestionTranslation(Schema schema) {
        Entity reviewQuestionTranslation = schema.addEntity("ReviewQuestionTranslation");
        reviewQuestionTranslation.addIdProperty().autoincrement();
        reviewQuestionTranslation.addStringProperty("questionHtml");
        reviewQuestionTranslation.addStringProperty("direction");
        reviewQuestionTranslation.addStringProperty("explanation");
        reviewQuestionTranslation.addStringProperty("language");
        return reviewQuestionTranslation;
    }

    private static Entity addReviewAnswer(Schema schema) {
        Entity reviewAnswer = schema.addEntity("ReviewAnswer");
        reviewAnswer.addLongProperty("id").primaryKey();
        reviewAnswer.addStringProperty("textHtml");
        reviewAnswer.addBooleanProperty("isCorrect");
        return reviewAnswer;
    }

    private static Entity addReviewAnswerTranslation(Schema schema) {
        Entity reviewAnswerTranslation = schema.addEntity("ReviewAnswerTranslation");
        reviewAnswerTranslation.addLongProperty("translationAnswerId").primaryKey().autoincrement();
        reviewAnswerTranslation.addLongProperty("id");
        reviewAnswerTranslation.addStringProperty("textHtml");
        reviewAnswerTranslation.addBooleanProperty("isCorrect");
        return reviewAnswerTranslation;
    }

    private static void addAttemptToReviewItem(Entity attempt, Entity reviewItem) {
        Property attemptId = reviewItem.addLongProperty("attemptId").getProperty();
        ToMany attemptToReviewItems = attempt.addToMany(reviewItem, attemptId);
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

}
