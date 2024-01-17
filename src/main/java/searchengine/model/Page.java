package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"site_id", "path"})})
public class Page implements Comparable<Page>
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    @Column(columnDefinition = "TEXT NOT NULL, Index(path(512))")
    private String path;
    private int code;
    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "idx",
            joinColumns = @JoinColumn(name = "page_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "lemma_id", referencedColumnName = "id")
    )
    private List<Lemma> lemmas;

    @Override
    public int compareTo(Page page) {
        int result = getSite().getId().compareTo(page.getSite().getId());
        if(result == 0) {
            result = getPath().compareTo(page.getPath());
        }
        return result;
    }
}
