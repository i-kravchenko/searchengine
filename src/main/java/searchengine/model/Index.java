package searchengine.model;

import lombok.Getter;
import lombok.Setter;

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
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;
    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "lemma_id", nullable = false)
    private Lemma lemma;
    @Column(nullable = false, name = "lemma_rank")
    private float rank;

}
