package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLInsert;

import javax.persistence.*;

@Entity
@Getter
@Setter
@SQLInsert(
        sql="INSERT INTO lemma(site_id, lemma, frequency) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE frequency = frequency + 1"
)
public class Lemma
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;
    @Column(nullable = false, columnDefinition = "int default 1")
    private int frequency = 1;
}
