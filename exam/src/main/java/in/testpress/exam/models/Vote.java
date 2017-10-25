package in.testpress.exam.models;

public class Vote<T> {

    private int id;
    private int typeOfVote;
    private T contentObject;
    private ProfileDetails voter;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTypeOfVote() {
        return typeOfVote;
    }

    public void setTypeOfVote(int typeOfVote) {
        this.typeOfVote = typeOfVote;
    }

    public T getContentObject() {
        return contentObject;
    }

    public void setContentObject(T contentObject) {
        this.contentObject = contentObject;
    }

    public ProfileDetails getVoter() {
        return voter;
    }

    public void setVoter(ProfileDetails voter) {
        this.voter = voter;
    }

    public Vote() {
    }
}