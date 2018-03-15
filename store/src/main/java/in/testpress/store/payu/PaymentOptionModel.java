package in.testpress.store.payu;

public class PaymentOptionModel {

    private int icon;
    private String name;

    public PaymentOptionModel() {
    }

    public PaymentOptionModel(int icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
