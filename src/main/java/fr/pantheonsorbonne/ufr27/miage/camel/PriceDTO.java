package fr.pantheonsorbonne.ufr27.miage.camel;
import jakarta.xml.bind.annotation.XmlRootElement;

public record PriceDTO(double price, ProductType productType) {
}
