
package com.tpinterprete.simple.interprete;
import java.io.IOException;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;

public class Main {

	private static final String EXTENSION = "smp";

	public static void main(String[] args) throws IOException {
		String program = args.length > 1 ? args[1] : "test/test." + EXTENSION;

		System.out.println("Interpreting file " + program);

		SimpleLexer lexer = new SimpleLexer(new ANTLRFileStream(program)); //Implementa el Analizador Lexico
		CommonTokenStream tokens = new CommonTokenStream(lexer); //Se toman los tokens y se pasan al Analizador Sintactico
		SimpleParser parser = new SimpleParser(tokens); //Implementa el Analizador Sintactico

		//Se ejecuta el Analizador Lexico, Analizador Sintactico y resulta en el Arbol de Sintaxis
		SimpleParser.ProgramContext tree = parser.program();

		SimpleCustomVisitor visitor = new SimpleCustomVisitor();
		visitor.visit(tree); //Se visitan los nodos del Arbol para realizar el Analisis Semantico

		System.out.println("Interpretation finished");

	}

}
