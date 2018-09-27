package com.ripple.core.types.shamap;

import com.ripple.core.types.known.tx.result.TransactionResult;

import java.util.TreeSet;

public class TransactionTree extends ShaMap {
    public TransactionTree() {
        super();
    }

    public TransactionTree(boolean isCopy, int depth) {
        super(isCopy, depth);
    }

    @Override
    protected ShaMapInner makeInnerOfSameClass(int depth) {
        return new TransactionTree(true, depth);
    }

    public void addTransactionResult(TransactionResult tr) {
        TransactionResultItem item = new TransactionResultItem(tr);
        addItem(tr.hash, item);
    }

    @Override
    public TransactionTree copy() {
        return (TransactionTree) super.copy();
    }

    public void walkTransactions(final TransactionResultVisitor walker) {
        walkLeaves(new LeafWalker() {
            @Override
            public void onLeaf(ShaMapLeaf shaMapLeaf) {
                TransactionResultItem item = (TransactionResultItem) shaMapLeaf.item;
                walker.onTransaction(item.result);
            }
        });
    }

    public TreeSet<TransactionResult> toTreeSet() {
        final TreeSet<TransactionResult> result = new TreeSet<>();
        walkTransactions(new TransactionResultVisitor() {
            @Override
            public void onTransaction(TransactionResult tx) {
                result.add(tx);
            }
        });
        return result;
    }
}
