import java.util.*;

public class Inventory {
    List<String> titleList;
    List<Integer> quantityList;
    List<List<String>> borrowerList;

    public Inventory(){
        titleList = new LinkedList<>();
        quantityList = new LinkedList<>();
        borrowerList = new LinkedList<>();
    }

    /**
     * Adds books to the inventory
     * @param title title of the book to be added
     * @param quantity quantity of books to add
     */
    public synchronized void addBook(String title, int quantity){
        titleList.add(title);
        quantityList.add(quantity);
        borrowerList.add(new LinkedList<>());
    }

    /**
     * Borrows a book from the inventory
     * @param title title of the book to be borrowed
     * @param borrower borrower's name
     * @return -1 if book not found, 0 if no copies left to borrow, 1 if successful borrow
     */
    public synchronized int borrowBook(String title, String borrower){
        //Check is available quantity is greater than 0
        int bookIndex = searchBook(title);
        if (bookIndex == -1)
            return -1;

        if (quantityList.get(bookIndex) == 0)
            return 0;

        quantityList.set(bookIndex, quantityList.get(bookIndex) - 1);
        borrowerList.get(bookIndex).add(borrower);
        return 1;
    }

    /**
     * Returns the index of the book in inventory
     * @param title title of the book to be found
     * @return index of book in inventory or -1 if not found
     */
    public int searchBook(String title){
        for (int i = 0; i < titleList.size(); i++){
            if (titleList.get(i).equals(title))
                return i;
        }
        return -1;
    }

    /**
     * Return the borrowed book in inventory
     * @param title title of book to be returned
     * @param borrower borrower who is returning the book
     * @return 1 if successfully returned, -1 if book does not exist in inventory
     */
    public synchronized int returnBook(String title, String borrower){
        int bookIndex = searchBook(title);
        if (bookIndex == -1)
            return -1;
        
        quantityList.set(bookIndex, quantityList.get(bookIndex) + 1);
        borrowerList.get(bookIndex).remove(borrower);
        return 1;
    }

    /**
     * Returns list of titles that borrower currently has
     * @param borrower name of borrower
     * @return list of titles that borrower currently has
     */
    public synchronized List<String> borrowerList(String borrower){
        List<String> list = new LinkedList<>();
        for (int i = 0; i < titleList.size(); i++){
            for (String name : borrowerList.get(i)){
                if (name.equals(borrower))
                    list.add(titleList.get(i));
            }
        }
        return list;
    }

    /**
     * Returns the inventory represented by a String
     * @return inventory represented by a String
     */
    public synchronized String printInventory(){
        String inventStr = "";

        for (int i = 0; i < titleList.size(); i++){
            inventStr += titleList.get(i) + " " + quantityList.get(i);
            if(i < titleList.size() - 1) {
                inventStr +="___";
            }
        }
        return inventStr;
    }
}
