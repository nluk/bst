package pl.nluk;

import java.util.*;

public class Main {

    static StringBuilderVisitor<Integer> stringBuilderVisitor = new StringBuilderVisitor<>();

    public static void main(String[] args) {
        Collection<Integer> randomInts = uniqueIntegers(10, 30);
        p("Wylosowano: "+randomInts);
        BST<Integer> bst = BST.ofComparable();
        randomInts.forEach(bst::add);
        bst.visit(stringBuilderVisitor, Visitor.Order.INORDER);
        p("Ścieżka inorder:");
        p(stringBuilderVisitor.getResult());
        stringBuilderVisitor.clear();
        findNumber(bst);
        p("Maksimum: "+bst.maxNode().getValue());
        p("Minimum: "+bst.minNode().getValue());
    }

    private static void p(String str){
        System.out.println(str);
    }

    private static Collection<Integer> uniqueIntegers(int n, int bound){
        Set<Integer> unique = new HashSet<>(n);
        Random r = new Random();
        while (unique.size() != n){
            int value = r.nextInt(bound);
            unique.add(value);
        }
        return unique;
    }

    private static void findNumber(BST<Integer> bst){
        Scanner scanner = new Scanner(System.in);
        String anotherSearch = "Y";
        while ("Y".equalsIgnoreCase(anotherSearch)){
            p("Jakiej liczby szukać? ");
            int search = scanner.nextInt();
            Node<Integer> result = bst.find(search, stringBuilderVisitor);
            if(result != null){
                p("Znaleziono, ścieżka od korzenia: ");
                p(stringBuilderVisitor.getResult());
            }
            else {
                p("Nie znaleziono");
            }
            stringBuilderVisitor.clear();
            p("Kolejna liczba? [Y/N]");
            anotherSearch = scanner.next();
        }
    }

    static class StringBuilderVisitor<T> implements Visitor<T>{
        StringBuilder sb = new StringBuilder();
        @Override
        public void visit(T t) {
            sb.append(t).append(", ");
        }

        public String getResult(){
            return sb.toString();
        }

        public void clear(){
            sb = new StringBuilder();
        }
    }

}
