package ec.utb.WebLibraryProject.controller;

import ec.utb.WebLibraryProject.data.AppUserRepository;
import ec.utb.WebLibraryProject.data.BookRepository;
import ec.utb.WebLibraryProject.data.LoanRepository;
import ec.utb.WebLibraryProject.dto.CreateLoanForm;
import ec.utb.WebLibraryProject.entity.AppUser;
import ec.utb.WebLibraryProject.entity.Book;
import ec.utb.WebLibraryProject.entity.Loan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static java.time.temporal.ChronoUnit.DAYS;

//Author: Benjamin Boson & Cheng Tao
@Controller
public class AppUserController {

    private BookRepository bookRepository;
    private AppUserRepository appUserRepository;
    private LoanRepository loanRepository;

    @Autowired
    public AppUserController(BookRepository bookRepository, AppUserRepository appUserRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.appUserRepository = appUserRepository;
        this.loanRepository = loanRepository;
    }

    @RequestMapping(value = "loans/{email}")
    public String getLoanView(Model model, @PathVariable("email") String email, @AuthenticationPrincipal UserDetails caller){
        if (caller == null){
            return "redirect:/accessDenied";
        }

        if (email.equals(caller.getUsername()) || caller.getAuthorities().stream().anyMatch(
                auth -> auth.getAuthority().equals("ADMIN"))){
            AppUser user = appUserRepository.findByEmailIgnoreCase(email).orElseThrow(
                    () -> new IllegalArgumentException("User could not be found")
            );
            model.addAttribute("loanList",user.getLoanList());
            return "loans-view";
        }else{
            return "access-denied";
        }
    }

    @GetMapping("/books")
    public String getBookView(Model model,@RequestParam(value = "search", defaultValue = "all") String search){
        System.out.println(search);
        List<Book> libraryBookList = new ArrayList<>();
//        Set the defaultValue as "all", so when at first time visiting this page without entering keyword, the whole list will be shown
        if (search.equals("all")){
            libraryBookList = bookRepository.findAll();
        }else{ //When entering some keyword, it will be changed to the following method
            libraryBookList = bookRepository.findByTitleContainsIgnoreCase(search);
            //When the list is empty, which means nothing matches the keyword or nothing found after entering keyword, a message will be shown
            if (libraryBookList.size() < 1){
                model.addAttribute("message","Your search '"+search+"' didn't match any book...");
            }
        }
        model.addAttribute("bookList",libraryBookList);
        return "books-view";
    }

    @GetMapping("/create/loan/{libraryBookId}")
    public String getCreateLoanForm(Model model, @PathVariable("libraryBookId") int bookId){
        model.addAttribute("form",new CreateLoanForm());
        model.addAttribute("bookId",bookId);
        Book book = bookRepository.findById(bookId).orElseThrow(IllegalArgumentException::new);
        model.addAttribute("book",book);
        return "create-loan";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/accessDenied")
    public String getAccessDenied(){
        return "access-denied";
    }

    @PostMapping("/create/loan/process")
    public String processCreateLoanForm(@Valid @ModelAttribute("form") CreateLoanForm form, BindingResult bindingResult, Model model){

        if (!form.getEndDate().isEmpty() && LocalDate.parse(form.getEndDate()).isBefore(LocalDate.parse(form.getStartDate()))){
            FieldError error = new FieldError("form","endDate","Return date can't be before initial loan date");
            bindingResult.addError(error);
        }

        if (!form.getEndDate().isEmpty() && LocalDate.parse(form.getStartDate()).isBefore(LocalDate.now())){
            FieldError error = new FieldError("form","startDate","Enter a date after "+LocalDate.now().minusDays(1));
            bindingResult.addError(error);
        }

        if (!form.getEndDate().isEmpty() && DAYS.between(LocalDate.parse(form.getStartDate()),LocalDate.parse(form.getEndDate()))
                > bookRepository.findById(Integer.valueOf(form.getBookId())).orElseThrow(IllegalArgumentException::new).getMaxLoanDays()
                && LocalDate.parse(form.getStartDate()).isBefore(LocalDate.parse(form.getEndDate()))){
            FieldError error = new FieldError("form","endDate","Loan period exceeds max loan days for this book");
            bindingResult.addError(error);
        }

        if (bindingResult.hasErrors()){
            model.addAttribute("form",form);
            model.addAttribute("bookId",Integer.valueOf(form.getBookId()));
            Book book = bookRepository.findById(Integer.valueOf(form.getBookId())).orElseThrow(IllegalArgumentException::new);
            model.addAttribute("book",book);
            return "create-loan";
        }

        AppUser user = appUserRepository.findByEmailIgnoreCase(form.getAppUserEmail()).orElseThrow(IllegalArgumentException::new);
        Book book = bookRepository.findById(Integer.valueOf(form.getBookId())).orElseThrow(IllegalAccessError::new);
        book.setAvailable(false);
        Loan loan = new Loan(LocalDate.parse(form.getStartDate()),LocalDate.parse(form.getEndDate()), user,book);
        user.addLoan(loan);
        loanRepository.save(loan);
        return "redirect:/index";
    }

//    @GetMapping("/search")
//    public String findBook(@RequestParam(value = "search", required = false) String title, Model model){
//        List<Book> bookList = bookRepository.findByTitleContainsIgnoreCase(title);
//        model.addAttribute("searchResult", bookList);
//        return "books-view";
//    }

    @GetMapping("loans/return/{id}")
    public String returnBook(@PathVariable("id") int loanId){
        Loan loan = loanRepository.findById(loanId).orElseThrow(IllegalArgumentException::new);
        loan.getAppUser().getLoanList().remove(loan);
        loan.getBook().setAvailable(true);
        loanRepository.delete(loan);
        return "redirect:/index";
    }
}
