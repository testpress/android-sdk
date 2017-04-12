package in.testpress;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;

import org.greenrobot.greendao.generator.Schema;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.ToMany;
import org.greenrobot.greendao.generator.ToOne;

public class TestpressDaoGenerator {
    // Increase the version if any modification has been made in this file.
    // Keep same version in all schemas.
    private static final int VERSION = 3;

    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(VERSION, "in.testpress.course.models.greendao");

        addCourse(schema);
        addChapter(schema);

        new DaoGenerator().generateAll(schema, "course/src/main/java");

        schema = new Schema(VERSION, "in.testpress.exam.models.greendao");

        Entity attempt = addAttempt(schema);
        Entity reviewItem = addReviewItem(schema);
        Entity reviewQuestion = addReviewQuestion(schema);
        Entity reviewAnswer = addReviewAnswer(schema);
        addSelectedAnswer(schema);
        addAttemptToReviewItem(attempt, reviewItem);
        addReviewItemToQuestion(reviewItem, reviewQuestion);
        addReviewQuestionToAnswers(reviewQuestion, reviewAnswer);

        new DaoGenerator().generateAll(schema, "exam/src/main/java");
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
                "List<Integer>",
                "IntegerListConverter"
        );
        reviewItem.addBooleanProperty("review");
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
        return reviewQuestion;
    }

    private static Entity addReviewAnswer(Schema schema) {
        Entity reviewAnswer = schema.addEntity("ReviewAnswer");
        reviewAnswer.addLongProperty("id").primaryKey();
        reviewAnswer.addStringProperty("textHtml");
        reviewAnswer.addBooleanProperty("isCorrect");
        return reviewAnswer;
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
}
