package model;

public class product {
	private String productId;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String category;
    
    
    public product(String productId, String name, String description, 
    		Double price,Integer stock, String category) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price= price;
        this.stock= stock;
        this.category=category;
    }
    
    
    public String getProductId(){return productId;}
    public String getName(){return name;}
    public String getDescription(){return description;}
    public Double getPrice(){return price;}
    public Integer getStock(){return stock;}
    public String getCategory(){return category;}

    public void setProductId(String productId){this.productId= productId;}
    public void setName(String name){this.name = name;}
    public void setDescription(String description){this.description =description;}
    public void setPrice(Double price){this.price= price;}
    public void setStock(Integer stock){this.stock = stock;}
    public void setCategory(String category){this.category = category;}
    
    // DTO Methods - Data Transfer Object conversion
    public String toDTO() {
        return String.format("%s|%s|%s|%.2f|%d|%s", 
            productId != null ? productId : "",
            name != null ? name : "",
            description != null ? description : "",
            price != null ? price : 0.0,
            stock != null ? stock : 0,
            category != null ? category : ""
        );
    }
    
    public static product fromDTO(String dtoString) {
        if (dtoString == null || dtoString.trim().isEmpty()) {
            return null;
        }
        
        String[] parts = dtoString.split("\\|");
        if (parts.length < 6) {
            return null;
        }
        
        try {
            String productId = parts[0].isEmpty() ? null : parts[0];
            String name = parts[1].isEmpty() ? null : parts[1];
            String description = parts[2].isEmpty() ? null : parts[2];
            Double price = parts[3].isEmpty() ? 0.0 : Double.parseDouble(parts[3]);
            Integer stock = parts[4].isEmpty() ? 0 : Integer.parseInt(parts[4]);
            String category = parts[5].isEmpty() ? null : parts[5];
            
            return new product(productId, name, description, price, stock, category);
        } catch (Exception e) {
            System.err.println("Error parsing product DTO: " + e.getMessage());
            return null;
        }
    }
    

}
