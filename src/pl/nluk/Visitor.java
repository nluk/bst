package pl.nluk;

interface Visitor<T> {

    enum Order{
        INORDER,
        PREORDER,
        POSTORDER
    }

    void visit(T t);
}
