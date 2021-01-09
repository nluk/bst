package pl.nluk;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BST<T> {

    private static final Random r = new Random();
    private static final Visitor<?> NO_OP = (n)->{};

    Comparator<T> comparator;
    Node<T> root;

    static <T2 extends Comparable<T2>> BST<T2> ofComparable(){
        return new BST<>(Comparable::compareTo);
    }

    static <T3> BST<T3> ofComparator(Comparator<T3> comparator){
        return new BST<>(comparator);
    }

    private BST(Comparator<T> comparator){
        this.comparator = comparator;
    }

    public void add(T value){
        if(root == null){
            root = new Node<>(value);
            return;
        }
        Node<T> currentNode = root;
        Consumer<Node<T>> nodeSetter;
        Supplier<Node<T>> nodeGetter;
        while (true){
            if (comparator.compare(value, currentNode.getValue()) < 0){
                nodeSetter = currentNode::setLeft;
                nodeGetter = currentNode::getLeft;
            }
            else {
                nodeSetter = currentNode::setRight;
                nodeGetter = currentNode::getRight;
            }
            if(nodeGetter.get() == null){
                Node<T> n = new Node<>(value);
                n.setParent(currentNode);
                nodeSetter.accept(n);
                break;
            }
            else {
                currentNode = nodeGetter.get();
            }
        }
    }


    public Node<T> find(T value, Visitor<T> visitor){
        Node<T> currentNode = root;
        while (true){
            if(currentNode == null){
                return null;
            }
            visitor.visit(currentNode.getValue());
            if(currentNode.getValue().equals(value)){
                return currentNode;
            }
            currentNode = comparator.compare(value, currentNode.getValue()) < 0 ? currentNode.getLeft() : currentNode.getRight();
        }
    }

    public Node<T> find(T value){
        //noinspection unchecked
        return find(value, (Visitor<T>) NO_OP);
    }

    private Node<T> predecessor(Node<T> from){
        if(from.getLeft() != null){
            return maxNode(from.getLeft());
        }
        Node<T> y;
        do {
            y = from;
            from = from.getParent();
        }while (from != null && (from.getRight() != y));

        return from;
    }

    private Node<T> successor(Node<T> from){
        if(from.getRight() != null){
            return minNode(from.getRight());
        }
        Node<T> y;
        do {
            y = from;
            from = from.getParent();
        }while (from != null && (from.getLeft() != y));
        return from;
    }

    public boolean remove(T t){
        Node<T> toRemove = find(t);
        if(toRemove != null){
            remove(toRemove);
            return true;
        }
        return false;
    }

    private Node<T> remove(Node<T> x){
        Node<T> z = null;
        Node<T> y = x.getParent();
        if(x.getLeft() != null && x.getRight() != null){
            z = r.nextBoolean() ? remove(predecessor(x)) : remove(successor(x));
            z.setLeft(x.getLeft());
            if(z.getLeft() != null){
                z.getLeft().setParent(z);
            }
            z.setRight(x.getRight());
            if(z.getRight() != null){
                z.getRight().setParent(z);
            }
        }
        else {
            z = (x.getLeft() != null) ? x.getLeft() : x.getRight();
        }
        if(z != null){
            z.setParent(y);
        }
        if(y == null){
            root = z;
        }
        else if(y.getLeft() == x){
            y.setLeft(z);
        }
        else {
            y.setRight(z);
        }
        return x;
    }

    private Node<T> traverseDirection(Node<T> from, Function<Node<T>, Node<T>> moveTo){
        if(from == null) return null;
        Node<T> currentNode = from;
        while (moveTo.apply(currentNode) != null){
            currentNode = moveTo.apply(currentNode);
        }
        return currentNode;
    }

    public Node<T> minNode(){
        return traverseDirection(root ,Node::getLeft);
    }

    public Node<T> maxNode(){
        return traverseDirection(root, Node::getRight);
    }

    public Node<T> minNode(Node<T> from){
        return traverseDirection(from ,Node::getLeft);
    }

    public Node<T> maxNode(Node<T> from){
        return traverseDirection(from, Node::getRight);
    }

    private void visitInorder(Visitor<T> visitor, Node<T> from){
        if(from == null) return;
        visitInorder(visitor, from.getLeft());
        visitor.visit(from.getValue());
        visitInorder(visitor, from.getRight());
    }

    private void visitPreorder(Visitor<T> visitor, Node<T> from){
        if(from == null) return;
        visitor.visit(from.getValue());
        visitPreorder(visitor, from.getLeft());
        visitPreorder(visitor, from.getRight());
    }

    private void visitPostorder(Visitor<T> visitor, Node<T> from){
        if(from == null) return;
        visitPostorder(visitor ,from.getLeft());
        visitPostorder(visitor, from.getRight());
        visitor.visit(from.getValue());
    }

    public void visit(Visitor<T> visitor, Visitor.Order order){
        switch (order){
            case INORDER:
                visitInorder(visitor, root);
                break;
            case PREORDER:
                visitPreorder(visitor, root);
                break;
            case POSTORDER:
                visitPostorder(visitor, root);
                break;
        }
    }

    public boolean isEmpty(){
        return root == null;
    }

    public void balanceDSW(){
        if (!isEmpty()) {
            createBackbone();
            createPerfectBST();
        }
    }

    private void createBackbone() {
        Node<T> grandParent = null;
        Node<T> parent = root;
        Node<T> leftChild;

        while (null != parent) {
            leftChild = parent.getLeft();
            if (null != leftChild) {
                grandParent = rotateRight(grandParent, parent, leftChild);
                parent = leftChild;
            } else {
                grandParent = parent;
                parent = parent.getRight();
            }
        }
    }

    private Node<T> rotateRight(Node<T> grandParent, Node<T> parent, Node<T> leftChild) {
        if (null != grandParent) {
            grandParent.setRight(leftChild);
        } else {
            root = leftChild;
        }
        parent.setLeft(leftChild.getRight());
        leftChild.setRight(parent);
        return grandParent;
    }

    private void createPerfectBST() {
        int n = 0;
        for (Node<T> tmp = root; null != tmp; tmp = tmp.getRight()) {
            n++;
        }
        //m = 2^floor[lg(n+1)]-1, ie the greatest power of 2 less than n: minus 1
        int m = greatestPowerOf2LessThanN(n + 1) - 1;
        makeRotations(n - m);
        while (m > 1) {
            makeRotations(m /= 2);
        }
    }

    private int greatestPowerOf2LessThanN(int n) {
        int x = MSB(n);//MSB
        return (1 << x);//2^x
    }

    private int MSB(int n) {
        int ndx = 0;
        while (1 < n) {
            n = (n >> 1);
            ndx++;
        }
        return ndx;
    }

    private void makeRotations(int bound) {
        Node<T> grandParent = null;
        Node<T> parent = root;
        Node<T> child = root.getRight();
        for (; bound > 0; bound--) {
            try {
                if (null != child) {
                    rotateLeft(grandParent, parent, child);
                    grandParent = child;
                    parent = grandParent.getRight();
                    child = parent.getRight();
                } else {
                    break;
                }
            } catch (NullPointerException convenient) {
                System.out.println("Co");
                break;
            }
        }
    }

    private void rotateLeft(Node<T> grandParent, Node<T> parent, Node<T> rightChild) {
        if (null != grandParent) {
            grandParent.setRight(rightChild);
        } else {
            root = rightChild;
        }
        parent.setRight(rightChild.getLeft());
        rightChild.setLeft(parent);
    }

    public Iterator<T> inorderIterator(){
        return new InorderIterator<>(root);
    }

    public Iterator<T> postorderIterator(){
        return new PostorderIterator<>(root);
    }

    public Iterator<T> preorderIterator(){
        return new PreorderIterator<>(root);
    }

    private static abstract class TraversalIterator<T> implements Iterator<T>{
        Node<T> current;
        Stack<Node<T>> stack = new Stack<>();

        @Override
        public boolean hasNext() {
            return current != null || !stack.empty();
        }

        TraversalIterator(Node<T> from){
            this.current = from;
        }
    }

    static  class PreorderIterator<T> extends TraversalIterator<T>{

        PreorderIterator(Node<T> from) {
            super(from);
            stack.push(from);
            current = null;
        }

        @Override
        public T next() {
            Node<T> node = stack.pop();
            if(node.getRight() != null){
                stack.push(node.getRight());
            }
            if(node.getLeft() != null){
                stack.push(node.getLeft());
            }
            return node.getValue();
        }
    }


    static class PostorderIterator<T> extends TraversalIterator<T>{
        PostorderIterator(Node<T> from){
            super(from);
        }

        @Override
        public T next() {
            T value = null;
            while (value == null){
                while (current != null){
                    if(current.getRight() != null){
                        stack.push(current.getRight());
                    }
                    stack.push(current);
                    current = current.getLeft();
                }
                current = stack.pop();
                if(current.getRight() != null && !stack.empty() && stack.peek() != null && current.getRight().equals(stack.peek())){
                    Node<T> child = stack.pop();
                    stack.push(current);
                    current = child;
                }
                else {
                    value = current.getValue();
                    current = null;
                }
            }
            return value;
        }
    }
    static class InorderIterator<T> extends TraversalIterator<T>{

        InorderIterator(Node<T> from){
            super(from);
        }

        @Override
        public T next() {
            T nextVal = null;
            while (current != null){
                stack.push(current);
                current = current.getLeft();
            }
            if(!stack.empty()){
                Node<T> top = stack.pop();
                nextVal = top.getValue();
                current = top.getRight();
            }
            return nextVal;
        }
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
