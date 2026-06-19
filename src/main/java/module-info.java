module org.example.restaurantgestion {
    requires java.xml;
    requires javafx.controls;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires static lombok;

    opens org.example.restaurantgestion.models;
    opens org.example.restaurantgestion;
    exports org.example.restaurantgestion;
}