package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"site_id", "lemma"})})
public class Lemma
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;
    private int frequency;
    @ManyToMany
    @JoinTable(
            name = "idx",
            joinColumns = @JoinColumn(name = "lemma_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "page_id", referencedColumnName = "id")
    )
    private List<Page> pages;
    @OneToMany(mappedBy = "lemma")
    private List<Index> indices;

    public float getPageRank(Page page) {
        Optional<Float> rank = indices.stream()
                .filter(index -> index.getPage().equals(page))
                .map(Index::getRank).findFirst();
        return rank.isPresent() ? rank.get() : 0;
    }
}
