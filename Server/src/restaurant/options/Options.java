package restaurant.options;

public enum Options {
    CreateTable("create"),
    Bill("bill"),
    RemoveFromTable("remove"),
    Order("order"),
    DeleteFromTable("delete"),
    RequestSomething("request"),
    ChangePassword("change"),
    Quit("end"),
    Ring("ring"),
    Null("Not supported"),
    Test("TEST");

    private final String option;

    Options(String option) {
        this.option = option;
    }

    public static Options getOption(String response) {
        for (Options option : Options.values()) {
            if (response.startsWith(option.option)) {
                return option;
            }
        }
        return Null;
    }

    @Override
    public String toString() {
        return option;
    }
}
