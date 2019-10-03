package tests;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import transfers.TransferRequestAnswer;
import transfers.TypeRequestAnswer;

import java.io.IOException;
import java.io.StringWriter;

public class TestJson implements TypeRequestAnswer {
    public static void main(String []args){
        TransferRequestAnswer out = new TransferRequestAnswer(AUTHORIZATION_DONE);
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        try {
            //objectMapper.writeValue(stringWriter,out);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(stringWriter,out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(stringWriter.toString());
    }
}
