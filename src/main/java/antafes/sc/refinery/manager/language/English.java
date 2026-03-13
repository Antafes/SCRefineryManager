package antafes.sc.refinery.manager.language;

public class English extends antafes.utilities.language.English
{
    public English()
    {
        super();
        this.general();
        this.newRefinement();
    }

    private void general()
    {
        this.getTranslations().put("title", "SC Refinery Manager");
    }

    private void newRefinement()
    {
        this.getTranslations().put("newRefinement", "Create new refinement");
    }
}
