package model;


public class ProductDTO {

    private String productId;
    private String name;
    private String description;
    private double price;
    private int    stock;
    private String category;

    public ProductDTO() {}

    public ProductDTO(String productId, String name, String description,
                      double price, int stock, String category) {
        this.productId   = productId;
        this.name        = name;
        this.description = description;
        this.price       = price;
        this.stock       = stock;
        this.category    = category;
    }

    // ── Getters / Setters ─────────────────────────────────────────
    public String getProductId()              { return productId; }
    public void   setProductId(String id)     { this.productId = id; }

    public String getName()                   { return name; }
    public void   setName(String name)        { this.name = name; }

    public String getDescription()            { return description; }
    public void   setDescription(String desc) { this.description = desc; }

    public double getPrice()                  { return price; }
    public void   setPrice(double price)      { this.price = price; }

    public int    getStock()                  { return stock; }
    public void   setStock(int stock)         { this.stock = stock; }

    public String getCategory()               { return category; }
    public void   setCategory(String cat)     { this.category = cat; }

   
    @Override
    public String toString() {
        return String.format(
            "ProductDTO{id='%s', name='%s', category='%s', price=%.2f, stock=%d}",
            productId, name, category, price, stock
        );
    }
}
