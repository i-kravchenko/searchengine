package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "idx")
public class Index
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Page page;
    @ManyToOne
    @JoinColumn(name = "lemma_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lemma lemma;
    @Column(nullable = false, name = "lemma_rank")
    private float rank;
}
