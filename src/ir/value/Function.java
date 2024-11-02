package ir.value;

import ir.IRBuilder;
import ir.type.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Function extends Value{
    private ArrayList<Argument> arguments = new ArrayList<>();
    private ArrayList<BasicBlock> blocks = new ArrayList<>();
    private HashMap<String,Value> variables = new HashMap<>();//语义分析符号表到函数内符号表达的映射

    private HashMap<String,String> tmpMap = new HashMap<>();//从符号表到原符号表的映射

    public Function(String name, Type type) {
        super(name, type);
    }

    public ArrayList<Argument> getArguments() {
        return arguments;
    }

    public void addArgument(Argument argument) {
        arguments.add(argument);
    }

    public void setArguments(ArrayList<Argument> arguments) {
        this.arguments = arguments;
    }

    public ArrayList<BasicBlock> getBlocks() {
        return blocks;
    }

    public void addBlock(BasicBlock block) {
        blocks.add(block);
    }

    public void addVariable(String key, Value value) {
        variables.put(key, value);
        tmpMap.put(value.getName(),key);
    }

    public Value getVariable(String key) {
        if(variables.containsKey(key)){
            return variables.get(key);
        }else{
            return IRBuilder.irModule.getGlobalVariable(key.substring(1));
        }
    }

    public void changeMap(Value oldValue,Value newValue) {
        variables.put(tmpMap.get(oldValue.getName()),newValue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("define "+getType()+" "+getName());
        sb.append("(");
        if(!arguments.isEmpty()){
            sb.append(arguments.get(0).toString());
            for(int i = 1; i < arguments.size(); i++){
                sb.append(", ").append(arguments.get(i).toString());
            }
        }
        sb.append(") {\n");
        for(BasicBlock b : blocks){
            sb.append(b.toString()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
