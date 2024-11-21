package midOPT;

import ir.value.BasicBlock;
import ir.value.Function;
import ir.value.IRModule;
import utils.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class DomTree {
    private IRModule irModule;
    private CFG cfg;
    public HashMap<BasicBlock, HashSet<BasicBlock>> doms = new HashMap<>();//A被哪些块支配
    public HashMap<BasicBlock, BasicBlock> idoms = new HashMap<>();//直接支配者(A被B支配)
    public HashMap<BasicBlock, HashSet<BasicBlock>> domTree = new HashMap<>();//支配树，A直接支配哪些块
    public HashMap<BasicBlock, HashSet<BasicBlock>> DF = new HashMap<>();//支配边界

    private HashSet<BasicBlock> visited = new HashSet<>();
    private BasicBlock deleted;

    public DomTree(IRModule irModule, CFG cfg) {
        this.irModule = irModule;
        this.cfg = cfg;
    }

    public DomTree(IRModule irModule) {
        this.irModule = irModule;
        this.cfg = new CFG(irModule);
        cfg.run();
    }

    public void run() {
        init();
        calDom();//计算支配者
        calIDom();//计算直接支配者
        calDomTree();//计算支配树
        calDF();//计算支配边界
    }

    public void init() {
        doms.clear();
        for (Function function : irModule.functions) {
            BasicBlock root = function.getBlocks().get(0);
            for (BasicBlock block : function.getBlocks()) {
                doms.put(block, new HashSet<>());
                doms.get(block).add(block);
                doms.get(block).add(root);
                DF.put(block, new HashSet<>());
                domTree.put(block, new HashSet<>());
            }
        }
    }

    public void calDom() {
        //节点删除法求支配结点
        for (Function function : irModule.functions) {
            BasicBlock root = function.getBlocks().get(0);
            for (BasicBlock block : function.getBlocks()) {
                if (block.equals(root)) {
                    continue;
                }
                visited.clear();
                deleted = block;
                visited.add(root);
                dfs(root);
                for (BasicBlock tmp : function.getBlocks()) {
                    if (!visited.contains(tmp)) {
                        doms.get(tmp).add(block);
                    }
                }
            }
        }

        //教程中的算法无法处理循环的情况
//        for (Function function : irModule.functions) {
//            boolean flag = true;
//            while (flag) {
//                flag = false;
//                for (BasicBlock block : function.getBlocks()) {
//                    HashSet<BasicBlock> tmp = new HashSet<>();
//                    Iterator<BasicBlock> it = cfg.prevs.get(block).iterator();//所有前驱
//                    if (it.hasNext()) {
//                        tmp.addAll(doms.get(it.next()));
//                    }
//                    while (it.hasNext()) {
//                        tmp.retainAll(doms.get(it.next()));//前驱的dom取交集
//                    }
//                    for (BasicBlock b : tmp) {
//                        if (!doms.get(block).contains(b)) {
//                            flag = true;
//                            doms.get(block).add(b);
//                        }
//                    }
//                }
//            }
//        }
    }

    public void dfs(BasicBlock block) {
        for (BasicBlock nextBlock : cfg.nexts.get(block)) {
            if (nextBlock.equals(deleted)) {
                continue;
            }
            if (!visited.contains(nextBlock)) {
                visited.add(nextBlock);
                dfs(nextBlock);
            }
        }
    }

    public void calIDom() {
        for (Function function : irModule.functions) {
            for (BasicBlock block : function.getBlocks()) {
                BasicBlock idom = null;
                for (BasicBlock dom : doms.get(block)) {
                    if (dom.equals(block)) {
                        continue;
                    }
                    if (idom == null) {
                        idom = dom;
                    } else if (doms.get(dom).contains(idom)) {
                        idom = dom;
                    }
                }
                idoms.put(block, idom);
            }
        }
    }

    public void calDomTree() {
        for (BasicBlock beDom : idoms.keySet()) {
//            if (!domTree.containsKey(idoms.get(beDom))) {
//                domTree.put(idoms.get(beDom), new HashSet<>());
//            }
            if (idoms.get(beDom) == null) {
                continue;
            }
            domTree.get(idoms.get(beDom)).add(beDom);
        }
    }

    public void calDF() {
        cfg.calEges();
        for (Pair<BasicBlock, BasicBlock> pair : cfg.edges) {
            BasicBlock a = pair.getFirst();
            BasicBlock b = pair.getSecond();
            BasicBlock x = a;
            while (!doms.get(b).contains(x) || x.equals(b)) {
                DF.get(x).add(b);
                x = idoms.get(x);
            }
        }
    }
}
