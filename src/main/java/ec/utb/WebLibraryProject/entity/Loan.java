package ec.utb.WebLibraryProject.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

//Author: Benjamin Boson & Lukas Rasmussen
@Entity
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int loanId;
    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.DETACH,CascadeType.MERGE,CascadeType.REFRESH}
    )
    private AppUser appUser;
    @OneToOne(
            cascade = {CascadeType.DETACH,CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH},
            fetch = FetchType.LAZY
    )
    private Book book;

    public Loan(LocalDate startDate, LocalDate endDate){
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Loan(LocalDate startDate, LocalDate endDate, AppUser appUser, Book book) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.appUser = appUser;
        this.book = book;
    }

    public Loan() {
    }

    public int getLoanId() {
        return loanId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loan loan = (Loan) o;
        return loanId == loan.loanId &&
                startDate.equals(loan.startDate) &&
                endDate.equals(loan.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loanId, startDate, endDate);
    }

    @Override
    public String toString() {
        return "Loan{" +
                "loanId=" + loanId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
