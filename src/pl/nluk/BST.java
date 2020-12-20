package pl.nluk;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BST<T> {

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

    private T traverseDirection(Function<Node<T>, Node<T>> moveTo){
        if(root == null) return null;
        Node<T> currentNode = root;
        while (moveTo.apply(currentNode) != null){
            currentNode = moveTo.apply(currentNode);
        }
        return currentNode.getValue();
    }

    public T min(){
        return traverseDirection(Node::getLeft);
    }

    public T max(){
        return traverseDirection(Node::getRight);
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
