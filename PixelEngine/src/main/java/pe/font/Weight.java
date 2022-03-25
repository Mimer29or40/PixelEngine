package pe.font;

public enum Weight
{
    THIN,
    EXTRA_LIGHT,
    LIGHT,
    REGULAR,
    MEDIUM,
    SEMI_BOLD,
    BOLD,
    EXTRA_BOLD,
    BLACK,
    ;
    
    private final String tag;
    
    Weight()
    {
        this.tag = format(name());
    }
    
    public String tag()
    {
        return tag;
    }
    
    public static Weight get(String weightString)
    {
        String str = format(weightString);
        for (Weight weight : Weight.values())
        {
            if (weight.tag().equals(str)) return weight;
        }
        return Weight.REGULAR;
    }
    
    private static String format(String weight)
    {
        return weight.replace("_", "").toLowerCase();
    }
}
