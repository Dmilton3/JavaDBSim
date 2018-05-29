import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ASUS on 10/12/2017.
 */
public class Driver {

    public static void main(String[] args) {

        Algebra reader = new Algebra();
        
        /*
        Test Case 1
        MAKE~MODEL~PRICE
        Toyota~Camry~18000
        Toyota~Tacoma~19000
        Ford~Mustang~21000
        Chevrolet~Corvette~48000
        Ford~F150~25000
        Toyota~Highlander~35000
         */

        reader.Project("cars.txt", "Make,Model,Price", "test1.txt");

        reader.Answer("test1.txt");

        /*
        Test Case 2
        MODEL~PRICE
        Camry~18000
        Tacoma~19000
        Mustang~21000
        Corvette~48000
        F150~25000
        Highlander~35000
         */
        reader.Project("cars.txt", "Model,Price", "test2.txt");

        reader.Answer("test2.txt");
        /*
        Test Case 3
        MAKE~MODEL
        Toyota~Camry
        Toyota~Tacoma
        Ford~Mustang
        Chevrolet~Corvette
        Ford~F150
        Toyota~Highlander
         */
        reader.Project("cars.txt", "Make,Model", "test3.txt");

        reader.Answer("test3.txt");

        reader.Project("cars.txt", "Model,Make,Price,Type", "test3.txt");

        reader.Answer("test3.txt");

        System.out.println("Begin Failed tests");
        reader.Project("cars.txt", "Make,tom", "test3.txt");
        reader.Project("cars.txt", "jack,Model", "test3.txt");
    //    reader.Restrict("lol.txt", "jack,Model", "test3.txt");
//        reader.Restrict("cars.txt", "jack,pass", "test3.txt");
 //       reader.Restrict("cars.txt", "Bill<=Rick", "test3.txt");
 //       reader.Restrict("cars.txt", "LLLLOOOLLLL", "test3.txt");

      //  System.out.println("Test answers");

      //  reader.Answer("cars.txt");
      //  reader.Answer("test1.txt");
      //  reader.Answer("test2.txt");
     //   reader.Answer("test3.txt");

        /*
           TestCase 4, joins cars, and owners table

         */
        reader.Join("cars.txt", "owners.txt", "ownership.txt");
        reader.Answer("ownership.txt");
        reader.Join("owners.txt", "cars.txt", "newOwnership.txt");
        reader.Answer("newOwnership.txt");

        reader.Restrict("ownership.txt", "Price>='19000'", "byRow.txt");
        reader.Answer("byRow.txt");
        reader.Restrict("ownership.txt", "Price='19000'", "priceTest.txt");
        reader.Answer("priceTest.txt");
        reader.Restrict("ownership.txt", "Price<'19000'", "priceTest2.txt");
        reader.Answer("priceTest2.txt");
        reader.Restrict("ownership.txt", "Owner='Jason Hickle'", "byCol.txt");
        reader.Answer("byCol.txt");
        reader.Restrict("ownership.txt", "Model=Model", "fun.txt");
        reader.Answer("fun.txt");
        reader.Restrict("ownership.txt", "Make='Toyota'", "toyota.txt");
        reader.Answer("toyota.txt");
        reader.Restrict("ownership.txt", "Owner>'D'", "alpha.txt");
        reader.Answer("alpha.txt");
        reader.Restrict("ownership.txt", "Owner<'J'", "alpha2.txt");
        reader.Answer("alpha2.txt");

      //Currently cleanup does not work
        reader.cleanUp();

    }


}
