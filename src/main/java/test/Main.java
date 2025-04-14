package test;

import entite.Forum;
import service.ForumService;
import util.DataSource;

import java.sql.Date;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.

        Forum p=new Forum("Testtt","test","tets","sd4f6", Date.valueOf("2003-04-20"),"123456","test");
        ForumService ps=new ForumService();
        ps.createPst(p);

        p.setTitle("Hello there");

        ps.update(p);

        System.out.println(p.getId());
        //ps.delete(p);
        ps.readAll().forEach(System.out::println);
    }
}