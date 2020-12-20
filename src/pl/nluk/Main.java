package pl.nluk;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        Visitor<Integer> printPath = (Integer i)-> System.out.print(i+", ");
        BST<Integer> bst = BST.ofComparable();
        Collection<Integer> randomInts = uniqueIntegers(10, 30);
        randomInts.forEach(bst::add);
        System.out.println(randomInts);
        bst.visit(printPath, Visitor.Order.INORDER);
        System.out.println();
        System.out.println("Max: "+bst.max());
        System.out.println("Min: "+bst.min());
        bst.find(randomInts.stream().skip(3).findFirst().get(), printPath);
    }

    static Collection<Integer> uniqueIntegers(int n, int bound){
        Set<Integer> unique = new HashSet<>(n);
        Random r = new Random();
        while (unique.size() != n){
            int value = r.nextInt(bound);
            unique.add(value);
        }
        return unique;
    }

}
