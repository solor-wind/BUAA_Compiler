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
            initVal.addExp(parseExp());
            while (tokens.peek().is(TokenType.COMMA)) {
                initVal.addComma(tokens.next());
                initVal.addExp(parseExp());
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
                stmt.setStmt1(parseStmt());
                break;
            case BREAKTK:
            case CONTINUETK:
                stmt.setKeyword(tokens.next());
                //error i
                if (!tokens.peek().is(TokenType.SEMICN)) {
                    addError("i");
                } else {
                    stmt.addIdent(tokens.next());
                }
                break;
            case RETURNTK:
                stmt.setKeyword(tokens.next());
                if (tokens.peek().is(TokenType.SEMICN)) {
                    //stmt.addIdent(tokens.next());
                } else {
                    stmt.addExp(parseExp());
                }
                //error i
                if (!tokens.peek().is(TokenType.SEMICN)) {
                    addError("i");
                } else {
                    stmt.addIdent(tokens.next());
                }
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
                break;
            case LBRACE:
                stmt.setBlock(parseBlock());
                break;
            default:
                iskey = false;
        }
        if (iskey) {
            return stmt;
        }
        boolean isAssign = false;
        int keyPos = 0;
        for (int i = 0; tokens.peek(i) != null; i++) {
            if (tokens.peek(i).is(TokenType.ASSIGN)) {
                isAssign = true;
                if (tokens.peek(i + 1).is(TokenType.GETINTTK) || tokens.peek(i + 1).is(TokenType.GETCHARTK)) {
                    keyPos = i + 1;
                }
            }
            if (tokens.peek(i).is(TokenType.SEMICN)) {
                break;
            }
        }
        if (!isAssign) {
            if (!tokens.peek().is(TokenType.SEMICN)) {
                stmt.addExp(parseExp());
            }
            //error i
            if (!tokens.peek().is(TokenType.SEMICN)) {
                addError("i");
            } else {
                stmt.addIdent(tokens.next());
            }
            return stmt;
        }
        if (keyPos != 0) {
            stmt.setKeyword(tokens.peek(keyPos));
            stmt.setLVal(parseLVal());
            stmt.addIdent(tokens.next());
            tokens.next();
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
        }
        stmt.setLVal(parseLVal());
        stmt.addIdent(tokens.next());//=
        stmt.addExp(parseExp());
        //error i
        if (!tokens.peek().is(TokenType.SEMICN)) {
            addError("i");
        } else {
            stmt.addIdent(tokens.next());
        }
        return stmt;
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
                if (tokens.peek().is(TokenType.LPARENT) || tokens.peek().is(TokenType.IDENFR)
                        || tokens.peek().is(TokenType.INTCON) || tokens.peek().is(TokenType.CHRCON)
                        || tokens.peek().is(TokenType.PLUS) || tokens.peek().is(TokenType.MINU) || tokens.peek().is(TokenType.NOT)) {
                    unaryExp.setFuncRParams(parseFuncRParams());
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
}