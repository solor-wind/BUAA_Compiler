package frontend.ast;

import frontend.ast.units.defs.*;
import frontend.ast.units.exps.ConstExp;
import frontend.ast.units.stmts.*;
import frontend.ast.units.exps.*;
import frontend.lexer.TokenStream;
import frontend.lexer.TokenType;

import java.util.TreeMap;

public class Parser {
    private TokenStream tokens;
    private CompUnit compUnit = new CompUnit();
    private TreeMap<Integer, String> errors;

    public Parser(TokenStream tokenStream) {
        this.tokens = tokenStream;
        errors = new TreeMap<>();
    }

    public CompUnit getCompUnit() {
        return compUnit;
    }

    public TreeMap<Integer, String> getErrors() {
        return errors;
    }

    public void parse() {
        //int a
        //const int a
        //int a()
        while (!tokens.peek(2).is(TokenType.LPARENT)) {
            if (tokens.peek().is(TokenType.CONSTTK)) {
                compUnit.addDecl(parseConstDecl());
            } else {
                compUnit.addDecl(parseVarDecl());
            }
        }
        while (tokens.peek() != null) {
            compUnit.addFuncDef(parseFuncDef());
        }
    }

    public ConstDecl parseConstDecl() {
        ConstDecl constDecl = new ConstDecl(tokens.next(), tokens.next());
        constDecl.addConstDef(parseConstDef());
        while (tokens.peek().is(TokenType.COMMA)) {
            constDecl.addCommas(tokens.next());
            constDecl.addConstDef(parseConstDef());
        }
        //error i
        if (!tokens.peek().is(TokenType.SEMICN)) {
            addError("i");
        } else {
            constDecl.setSemicn(tokens.next());
        }
        return constDecl;
    }

    public ConstDef parseConstDef() {
        ConstDef constDef = new ConstDef(tokens.next());
        if (tokens.peek().is(TokenType.LBRACK)) {
            constDef.setLbrack(tokens.next());
            constDef.setConstExp(parseConstExp());
            //error k
            if (!tokens.peek().is(TokenType.RBRACK)) {
                addError("k");
            } else {
                constDef.setRbrack(tokens.next());
            }
        }
        constDef.setAssign(tokens.next());
        constDef.setConstInitVal(parseConstInitVal());
        return constDef;
    }

    public ConstInitVal parseConstInitVal() {
        ConstInitVal constInitVal = new ConstInitVal();
        if (tokens.peek().is(TokenType.STRCON)) {
            constInitVal.setStringConst(tokens.next());
        } else if (tokens.peek().is(TokenType.LBRACE)) {
            constInitVal.setLbrace(tokens.next());//{
            if (!tokens.peek().is(TokenType.RBRACE)) {
                constInitVal.addConstExp(parseConstExp());
                while (tokens.peek().is(TokenType.COMMA)) {
                    constInitVal.addCommas(tokens.next());
                    constInitVal.addConstExp(parseConstExp());
                }
            }
            constInitVal.setRbrace(tokens.next());//}
        } else {
            constInitVal.addConstExp(parseConstExp());
        }
        return constInitVal;
    }

    public VarDecl parseVarDecl() {
        VarDecl varDecl = new VarDecl(tokens.next());
        varDecl.addVarDef(parseVarDef());
        while (tokens.peek().is(TokenType.COMMA)) {
            varDecl.addCommas(tokens.next());
            varDecl.addVarDef(parseVarDef());
        }
        //error i
        if (!tokens.peek().is(TokenType.SEMICN)) {
            addError("i");
        } else {
            varDecl.setSemicn(tokens.next());
        }
        return varDecl;
    }

    public VarDef parseVarDef() {
        VarDef varDef = new VarDef(tokens.next());
        if (tokens.peek().is(TokenType.LBRACK)) {
            varDef.setLbrack(tokens.next());//[
            varDef.setConstExp(parseConstExp());
            //error k
            if (!tokens.peek().is(TokenType.RBRACK)) {
                addError("k");
            } else {
                varDef.setRbrack(tokens.next());
            }
        }
        if (tokens.peek().is(TokenType.ASSIGN)) {
            varDef.setAssign(tokens.next());
            varDef.setInitVal(parseInitVal());
        }
        return varDef;
    }

    public InitVal parseInitVal() {
        InitVal initVal = new InitVal();
        if (tokens.peek().is(TokenType.STRCON)) {
            initVal.setStringConst(tokens.next());
        } else if (tokens.peek().is(TokenType.LBRACE)) {
            initVal.setLbrace(tokens.next());//{
            if (!tokens.peek().is(TokenType.RBRACE)) {
                initVal.addExp(parseExp());
                while (tokens.peek().is(TokenType.COMMA)) {
                    initVal.addComma(tokens.next());
                    initVal.addExp(parseExp());
                }
            }
            initVal.setRbrace(tokens.next());//}
        } else {
            initVal.addExp(parseExp());
        }
        return initVal;
    }

    public FuncDef parseFuncDef() {
        FuncDef funcDef = new FuncDef(tokens.next(), tokens.next());
        funcDef.setLparent(tokens.next());//(
        if (!tokens.peek().is(TokenType.RPARENT)) {
            if (tokens.peek().is(TokenType.LBRACE)) {
                //error j
                addError("j");
                funcDef.setBlock(parseBlock());
                return funcDef;
            } else {
                funcDef.setFuncFParams(parseFuncFParams());
            }
        }
        //error j
        if (!tokens.peek().is(TokenType.RPARENT)) {
            addError("j");
        } else {
            funcDef.setRparent(tokens.next());
        }
        funcDef.setBlock(parseBlock());
        return funcDef;
    }

    public FuncFParams parseFuncFParams() {
        FuncFParams funcFParams = new FuncFParams();
        funcFParams.addFuncFParam(parseFuncFParam());
        while (tokens.peek().is(TokenType.COMMA)) {
            funcFParams.addComma(tokens.next());
            funcFParams.addFuncFParam(parseFuncFParam());
        }
        return funcFParams;
    }

    public FuncFParam parseFuncFParam() {
        FuncFParam funcFParam = new FuncFParam(tokens.next(), tokens.next());
        if (tokens.peek().is(TokenType.LBRACK)) {
            funcFParam.setLbrack(tokens.next());//[
            //error k
            if (!tokens.peek().is(TokenType.RBRACK)) {
                addError("k");
            } else {
                funcFParam.setRbrack(tokens.next());
            }
        }
        return funcFParam;
    }

    public Block parseBlock() {
        Block block = new Block();
        block.setLbrace(tokens.next());//{
        while (!tokens.peek().is(TokenType.RBRACE)) {
            block.addBlockItem(parseBlockItem());
        }
        block.setRbrace(tokens.next());//}
        return block;
    }

    public BlockItem parseBlockItem() {
        if (tokens.peek().is(TokenType.CONSTTK)) {
            return parseConstDecl();
        } else if (tokens.peek().is(TokenType.INTTK) || tokens.peek().is(TokenType.CHARTK)) {
            return parseVarDecl();
        } else {
            return parseStmt();
        }
    }

    private int forFloor = 0;

    public Stmt parseStmt() {
        Stmt stmt = new Stmt(null);
        boolean iskey = true;
        switch (tokens.peek().getToken()) {
            case IFTK:
                stmt.setKeyword(tokens.next());
                stmt.addIdent(tokens.next());//(
                stmt.setCond(parseCond());
                //error j
                if (!tokens.peek().is(TokenType.RPARENT)) {
                    addError("j");
                } else {
                    stmt.addIdent(tokens.next());
                }
                stmt.setStmt1(parseStmt());
                if (tokens.peek().is(TokenType.ELSETK)) {
                    stmt.addIdent(tokens.next());
                    stmt.setStmt2(parseStmt());
                }
                stmt.setKind(4);
                break;
            case FORTK:
                stmt.setKeyword(tokens.next());
                stmt.addIdent(tokens.next());//(
                if (!tokens.peek().is(TokenType.SEMICN)) {
                    stmt.setForStmt1(parseForStmt());
                }
                stmt.addIdent(tokens.next());//;
                if (!tokens.peek().is(TokenType.SEMICN)) {
                    stmt.setCond(parseCond());
                }
                stmt.addIdent(tokens.next());//;
                if (!tokens.peek().is(TokenType.RPARENT)) {
                    stmt.setForStmt2(parseForStmt());
                }
                stmt.addIdent(tokens.next());//)
                forFloor++;
                stmt.setStmt1(parseStmt());
                forFloor--;
                stmt.setKind(5);
                break;
            case BREAKTK:
            case CONTINUETK:
                //error m
                if (forFloor == 0) {
                    errors.put(tokens.peek().getLine(), "m");
                }
                stmt.setKeyword(tokens.next());
                //error i
                if (!tokens.peek().is(TokenType.SEMICN)) {
                    addError("i");
                } else {
                    stmt.addIdent(tokens.next());
                }
                stmt.setKind(6);
                break;
            case RETURNTK:
                stmt.setKeyword(tokens.next());
                if (tokens.peek().is(TokenType.SEMICN)) {
                    //stmt.addIdent(tokens.next());
                } else {
                    Exp exp = tryExp();
                    if (exp != null) {
                        stmt.addExp(exp);
                    }
                }
                //error i
                if (!tokens.peek().is(TokenType.SEMICN)) {
                    addError("i");
                } else {
                    stmt.addIdent(tokens.next());
                }
                stmt.setKind(7);
                break;
            case PRINTFTK:
                stmt.setKeyword(tokens.next());
                stmt.addIdent(tokens.next());//(
                stmt.setStringConst(tokens.next());
                while (tokens.peek().is(TokenType.COMMA)) {
                    stmt.addIdent(tokens.next());//,
                    stmt.addExp(parseExp());
                }
                //error j
                if (!tokens.peek().is(TokenType.RPARENT)) {
                    addError("j");
                } else {
                    stmt.addIdent(tokens.next());
                }
                //error i
                if (!tokens.peek().is(TokenType.SEMICN)) {
                    addError("i");
                } else {
                    stmt.addIdent(tokens.next());
                }
                stmt.setKind(10);
                break;
            case LBRACE:
                stmt.setBlock(parseBlock());
                stmt.setKind(3);
                break;
            default:
                iskey = false;
        }
        if (iskey) {
            return stmt;
        }
        int pos = tokens.getPos();
        LVal lVal = tryLVal();
        if (lVal != null && tokens.peek().is(TokenType.ASSIGN)) {
            stmt.setLVal(lVal);
            stmt.addIdent(tokens.next());//=
            if (tokens.peek().is(TokenType.GETINTTK) || tokens.peek().is(TokenType.GETCHARTK)) {
                if (tokens.peek().is(TokenType.GETINTTK)) {
                    stmt.setKind(8);
                } else {
                    stmt.setKind(9);
                }
                stmt.setKeyword(tokens.next());
                stmt.addIdent(tokens.next());//(
                //error j
                if (!tokens.peek().is(TokenType.RPARENT)) {
                    addError("j");
                } else {
                    stmt.addIdent(tokens.next());
                }
                //error i
                if (!tokens.peek().is(TokenType.SEMICN)) {
                    addError("i");
                } else {
                    stmt.addIdent(tokens.next());
                }
                return stmt;
            } else {
                stmt.addExp(parseExp());
                //error i
                if (!tokens.peek().is(TokenType.SEMICN)) {
                    addError("i");
                } else {
                    stmt.addIdent(tokens.next());
                }
                stmt.setKind(1);
                return stmt;
            }
        } else {
            tokens.setPos(pos);
            stmt.setKind(2);
            if (tokens.peek().is(TokenType.SEMICN)) {
                stmt.addIdent(tokens.next());
                return stmt;
            } else {
                Exp exp = tryExp();
                if (exp != null) {
                    stmt.addExp(exp);
                    //error i
                    if (!tokens.peek().is(TokenType.SEMICN)) {
                        addError("i");
                    } else {
                        stmt.addIdent(tokens.next());
                    }
                    return stmt;
                } else {
                    addError("i");
                    return stmt;
                }
            }
        }
    }

    public ForStmt parseForStmt() {
        ForStmt forStmt = new ForStmt();
        forStmt.setlVal(parseLVal());
        forStmt.setAssign(tokens.next());//=
        forStmt.setExp(parseExp());
        return forStmt;
    }

    public Exp parseExp() {
        return new Exp(parseAddExp());
    }

    public Cond parseCond() {
        return parseLOrExp();
    }

    public LVal parseLVal() {
        LVal lVal = new LVal(tokens.next());
        if (tokens.peek().is(TokenType.LBRACK)) {
            lVal.setLbrack(tokens.next());
            lVal.setExp(parseExp());
            //error k
            if (!tokens.peek().is(TokenType.RBRACK)) {
                addError("k");
            } else {
                lVal.setRbrack(tokens.next());
            }
        }
        return lVal;
    }

    public PrimaryExp parsePrimaryExp() {
        PrimaryExp primaryExp;
        if (tokens.peek().is(TokenType.LPARENT)) {
            primaryExp = new PrimaryExp();
            primaryExp.setLparent(tokens.next());
            primaryExp.setExp(parseExp());
            //error j
            if (!tokens.peek().is(TokenType.RPARENT)) {
                addError("j");
            } else {
                primaryExp.setRparent(tokens.next());
            }
        } else if (tokens.peek().is(TokenType.INTCON)) {
            primaryExp = new PrimaryExp(new IntConst(tokens.next()));
        } else if (tokens.peek().is(TokenType.CHRCON)) {
            primaryExp = new PrimaryExp(new CharConst(tokens.next()));
        } else {
            primaryExp = new PrimaryExp(parseLVal());
        }
        return primaryExp;
    }

    public UnaryExp parseUnaryExp() {
        UnaryExp unaryExp = new UnaryExp();
        while (tokens.peek().is(TokenType.NOT) || tokens.peek().is(TokenType.PLUS) || tokens.peek().is(TokenType.MINU)) {
            unaryExp.addUnaryOp(new UnaryOp(tokens.next()));
        }
        if (tokens.peek().is(TokenType.IDENFR) && tokens.peek(1).is(TokenType.LPARENT)) {
            unaryExp.setIdent(tokens.next());
            unaryExp.setLparent(tokens.next());//(
            if (!tokens.peek().is(TokenType.RPARENT)) {
                FuncRParams funcRParams = tryFuncRParams();
                if (funcRParams != null) {
                    unaryExp.setFuncRParams(funcRParams);
                } else {
                    //error j
                    addError("j");
                    return unaryExp;
                }
            }
            //error j
            if (!tokens.peek().is(TokenType.RPARENT)) {
                addError("j");
            } else {
                unaryExp.setRparent(tokens.next());
            }
        } else {
            unaryExp.setPrimaryExp(parsePrimaryExp());
        }
        return unaryExp;
    }

    public FuncRParams parseFuncRParams() {
        FuncRParams funcRParams = new FuncRParams();
        funcRParams.addExp(parseExp());
        while (tokens.peek().is(TokenType.COMMA)) {
            funcRParams.addComma(tokens.next());
            funcRParams.addExp(parseExp());
        }
        return funcRParams;
    }

    public MulExp parseMulExp() {
        MulExp mulExp = new MulExp();
        mulExp.addUnaryExp(parseUnaryExp());
        while (tokens.peek().is(TokenType.MULT) ||
                tokens.peek().is(TokenType.DIV) || tokens.peek().is(TokenType.MOD)) {
            mulExp.addOp(tokens.next());
            mulExp.addUnaryExp(parseUnaryExp());
        }
        return mulExp;
    }

    public AddExp parseAddExp() {
        AddExp addExp = new AddExp();
        addExp.addmulExp(parseMulExp());
        while (tokens.peek().is(TokenType.PLUS) || tokens.peek().is(TokenType.MINU)) {
            addExp.addOp(tokens.next());
            addExp.addmulExp(parseMulExp());
        }
        return addExp;
    }

    public RelExp parseRelExp() {
        RelExp relExp = new RelExp();
        relExp.addAddExp(parseAddExp());
        while (tokens.peek().is(TokenType.LSS) || tokens.peek().is(TokenType.LEQ) ||
                tokens.peek().is(TokenType.GRE) || tokens.peek().is(TokenType.GEQ)) {
            relExp.addOp(tokens.next());
            relExp.addAddExp(parseAddExp());
        }
        return relExp;
    }

    public EqExp parseEqExp() {
        EqExp eqExp = new EqExp();
        eqExp.addRelExp(parseRelExp());
        while (tokens.peek().is(TokenType.EQL) || tokens.peek().is(TokenType.NEQ)) {
            eqExp.addOp(tokens.next());
            eqExp.addRelExp(parseRelExp());
        }
        return eqExp;
    }

    public LAndExp parseLAndExp() {
        LAndExp landExp = new LAndExp();
        landExp.addEqExp(parseEqExp());
        while (tokens.peek().is(TokenType.AND) ||
                (tokens.peek().is(TokenType.ERROR) && tokens.peek().getValue().equals("&"))) {
            if (tokens.peek().is(TokenType.ERROR)) {
                //error a
                errors.put(tokens.peek().getLine(), "a");
            }
            landExp.addOp(tokens.next());
            landExp.addEqExp(parseEqExp());
        }
        return landExp;
    }

    public LOrExp parseLOrExp() {
        LOrExp lOrExp = new LOrExp();
        lOrExp.addLAndExp(parseLAndExp());
        while (tokens.peek().is(TokenType.OR) ||
                (tokens.peek().is(TokenType.ERROR) && tokens.peek().getValue().equals("|"))) {
            if (tokens.peek().is(TokenType.ERROR)) {
                //error a
                errors.put(tokens.peek().getLine(), "a");
            }
            lOrExp.addOp(tokens.next());
            lOrExp.addLAndExp(parseLAndExp());
        }
        return lOrExp;
    }

    public ConstExp parseConstExp() {
        return new ConstExp(parseAddExp());/*TODO必须是常量*/
    }

    public boolean inExpFirst() {
        if (tokens.peek() == null) {
            return false;
        }
        return tokens.peek().is(TokenType.LPARENT) || tokens.peek().is(TokenType.IDENFR)
                || tokens.peek().is(TokenType.INTCON) || tokens.peek().is(TokenType.CHRCON)
                || tokens.peek().is(TokenType.PLUS) || tokens.peek().is(TokenType.MINU) || tokens.peek().is(TokenType.NOT);
    }

    public void addError(String error) {
        if (tokens.peek(-1) == null) {
            errors.put(0, error);
        } else {
            errors.put(tokens.peek(-1).getLine(), error);
        }
    }

    public boolean isError() {
        return !errors.isEmpty();
    }

    @Override
    public String toString() {
        if (errors.isEmpty()) {
            return compUnit.toString();
        }
        StringBuilder sb = new StringBuilder();
        for (int i : errors.keySet()) {
            sb.append(i + " " + errors.get(i) + "\n");
        }
        return sb.toString();
    }

    public Exp tryExp() {
        if (!inExpFirst()) {
            return null;
        }
        return new Exp(tryAddExp());
    }

    public LVal tryLVal() {
        //支持回溯
        int pos = tokens.getPos();
        if (!tokens.peek().is(TokenType.IDENFR)) {
            return null;
        }
        LVal lVal = new LVal(tokens.next());
        if (tokens.peek().is(TokenType.LBRACK)) {
            lVal.setLbrack(tokens.next());
            Exp exp = tryExp();
            if (exp != null) {
                lVal.setExp(exp);
            } else {
                tokens.setPos(pos);
                return null;
            }
            //error k
            if (!tokens.peek().is(TokenType.RBRACK)) {
                addError("k");
            } else {
                lVal.setRbrack(tokens.next());
            }
        }
        return lVal;
    }

    public PrimaryExp tryPrimaryExp() {
        //支持回溯
        PrimaryExp primaryExp = null;
        int pos = tokens.getPos();
        if (tokens.peek().is(TokenType.LPARENT)) {
            primaryExp = new PrimaryExp();
            primaryExp.setLparent(tokens.next());
            Exp exp = tryExp();
            if (exp != null) {
                primaryExp.setExp(exp);
                //error j
                if (!tokens.peek().is(TokenType.RPARENT)) {
                    addError("j");
                } else {
                    primaryExp.setRparent(tokens.next());
                }
            } else {
                tokens.setPos(pos);
            }
        } else if (tokens.peek().is(TokenType.INTCON)) {
            primaryExp = new PrimaryExp(new IntConst(tokens.next()));
        } else if (tokens.peek().is(TokenType.CHRCON)) {
            primaryExp = new PrimaryExp(new CharConst(tokens.next()));
        } else {
            LVal lVal = tryLVal();
            if (lVal != null) {
                primaryExp = new PrimaryExp(lVal);
            }
        }
        return primaryExp;
    }

    public UnaryExp tryUnaryExp() {
        //支持回溯
        UnaryExp unaryExp = new UnaryExp();
        int pos = tokens.getPos();
        while (tokens.peek().is(TokenType.NOT) || tokens.peek().is(TokenType.PLUS) || tokens.peek().is(TokenType.MINU)) {
            unaryExp.addUnaryOp(new UnaryOp(tokens.next()));
        }
        if (tokens.peek().is(TokenType.IDENFR) && tokens.peek(1).is(TokenType.LPARENT)) {
            unaryExp.setIdent(tokens.next());
            unaryExp.setLparent(tokens.next());//(
            if (!tokens.peek().is(TokenType.RPARENT)) {
                FuncRParams funcRParams = tryFuncRParams();
                if (funcRParams != null) {
                    unaryExp.setFuncRParams(funcRParams);
                } else {
                    //error j
                    addError("j");
                    return unaryExp;
                }
            }
            //error j
            if (!tokens.peek().is(TokenType.RPARENT)) {
                addError("j");
            } else {
                unaryExp.setRparent(tokens.next());
            }
        } else {
            PrimaryExp primaryExp = tryPrimaryExp();
            if (primaryExp != null) {
                unaryExp.setPrimaryExp(primaryExp);
            } else {
                tokens.setPos(pos);
                return null;
            }
        }
        return unaryExp;
    }

    public FuncRParams tryFuncRParams() {
        //支持回溯
        FuncRParams funcRParams = new FuncRParams();
        int pos = tokens.getPos();
        Exp exp = tryExp();
        if (exp != null) {
            funcRParams.addExp(exp);
        } else {
            return null;
        }
        while (tokens.peek().is(TokenType.COMMA)) {
            funcRParams.addComma(tokens.next());
            exp = tryExp();
            if (exp != null) {
                funcRParams.addExp(exp);
            } else {
                tokens.setPos(pos);
                return null;
            }
        }
        return funcRParams;
    }

    public MulExp tryMulExp() {
        MulExp mulExp = new MulExp();
        int pos = tokens.getPos();
        UnaryExp unaryExp = tryUnaryExp();
        if (unaryExp != null) {
            mulExp.addUnaryExp(unaryExp);
        } else {
            return null;
        }
        while (tokens.peek().is(TokenType.MULT) ||
                tokens.peek().is(TokenType.DIV) || tokens.peek().is(TokenType.MOD)) {
            mulExp.addOp(tokens.next());
            unaryExp = tryUnaryExp();
            if (unaryExp != null) {
                mulExp.addUnaryExp(unaryExp);
            } else {
                tokens.setPos(pos);
                return null;
            }
        }
        return mulExp;
    }

    public AddExp tryAddExp() {
        if (!inExpFirst()) {
            return null;
        }
        AddExp addExp = new AddExp();
        int pos = tokens.getPos();
        MulExp mulExp = tryMulExp();
        if (mulExp != null) {
            addExp.addmulExp(mulExp);
        } else {
            return null;
        }
        while (tokens.peek().is(TokenType.PLUS) || tokens.peek().is(TokenType.MINU)) {
            addExp.addOp(tokens.next());
            mulExp = tryMulExp();
            if (mulExp != null) {
                addExp.addmulExp(mulExp);
            } else {
                tokens.setPos(pos);
                return null;
            }
        }
        return addExp;
    }
}