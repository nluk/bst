package pl.nluk;

import java.util.Comparator;
import java.util.Random;
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
}
