package com.tpinterprete.simple.interprete;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class HandleErrorsSintax extends BaseErrorListener { //Implemento mi propio manejador de errores lexicos y sintactico

    public static final HandleErrorsSintax INSTANCE = new HandleErrorsSintax();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
            throws ParseCancellationException {

        String mymsg = msg;

        if(mymsg.contains("token recognition error")) {
            mymsg = "La sintaxis es invalida, token no reconocido.";
        } else if(mymsg.contains("extraneous input")) {
            mymsg = "La sintaxis es invalida, entrada incorrecta o inesperada";
        } else if(mymsg.contains("mismatched input")) {
            mymsg = "La sintaxis es invalida, sintaxis o simbolo incompatible";
        } else if(mymsg.contains("missing")) {
            mymsg = "La sintaxis es invalida, falta un elemento obligatorio.";
        }

        throw new ParseCancellationException("[Linea: " + line + "] " + "ERROR: " + mymsg);
    }


}
