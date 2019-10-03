package tests;

import com.evgeny.MessageWorker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InputTest {
    @Test
    void checkLogin_001(){
        MessageWorker messageWorker = new MessageWorker(null, null);
        assertEquals(true,messageWorker.checkLogin("AbCdEfG_zaza"));
    }

    @Test
    void checkLogin_002(){
        MessageWorker messageWorker = new MessageWorker(null, null);
        assertEquals(true,messageWorker.checkLogin("AbCdEfG_zaza_"));
    }

    @Test
    void checkLogin_003(){
        MessageWorker messageWorker = new MessageWorker(null, null);
        assertEquals(true,messageWorker.checkLogin("AbCdEf4G_zaza_"));
    }

    @Test
    void checkLogin_004(){
        MessageWorker messageWorker = new MessageWorker(null, null);
        assertEquals(true,messageWorker.checkLogin("AbCdEfG_zaza_4"));
    }

    @Test
    void checkLogin_005(){
        MessageWorker messageWorker = new MessageWorker(null, null);
        assertEquals(true,messageWorker.checkLogin("4AbCdEfG_zaza_"));
    }

    @Test
    void checkLogin_006(){
        MessageWorker messageWorker = new MessageWorker(null, null);
        assertEquals(false,messageWorker.checkLogin("4AbCdEfЫG_zaza_"));
    }

    @Test
    void checkLogin_007(){
        MessageWorker messageWorker = new MessageWorker(null, null);
        assertEquals(false,messageWorker.checkLogin("4AbCdEf!G_zaza_"));
    }

    @Test
    void checkLogin_008(){
        MessageWorker messageWorker = new MessageWorker(null, null);
        assertEquals(false,messageWorker.checkLogin("4AbCdEf.G_zaza_"));
    }

    @Test
    void checkLogin_009(){
        MessageWorker messageWorker = new MessageWorker(null, null);
        assertEquals(false,messageWorker.checkLogin("4AbCdEf,G_zaza_"));
    }

    @Test
    void checkLogin_010(){
        MessageWorker messageWorker = new MessageWorker(null, null);
        assertEquals(true,messageWorker.checkLogin("AAABBBCCC"));
    }

    @Test
    void checkLogin_011(){
        MessageWorker messageWorker = new MessageWorker(null, null);
        assertEquals(true,messageWorker.checkLogin("aaabbbccc"));
    }

    @Test
    void checkLogin_012(){
        MessageWorker messageWorker = new MessageWorker(null, null);
        assertEquals(true,messageWorker.checkLogin("____________"));
    }

    @Test
    void checkLogin_013(){
        MessageWorker messageWorker = new MessageWorker(null, null);
        assertEquals(true,messageWorker.checkLogin("kafhcgakshjdcfgkdhsfgjkndhkchc"));
    }

    @Test
    void checkLogin_014(){
        MessageWorker messageWorker = new MessageWorker(null, null);
        assertEquals(false,messageWorker.checkLogin("kafhcgakshjdcfgkdhsfgjkndhkchcr"));
    }
}
