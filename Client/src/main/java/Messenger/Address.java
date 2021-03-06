package Messenger;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Utils;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * Created by chris on 1/26/15.
 */
public class Address {

    //Address format: base58encode(<version: 1><prefix_length: 1><public_key: 33><checksum: 4>)

    private ECKey key;
    public static int version = 0;
    private int prefixLength;
    private byte[] prefix = new byte[4];
    private byte[] publicKey = new byte[33];
    private byte[] checksum = new byte[4];
    private byte[] versionedchecksummed;

    public Address(int prefixLength) throws InvalidPrefixLengthException{
        if (prefixLength > 32) throw new InvalidPrefixLengthException();
        ECKey key = new ECKey();
        this.key = key;
        createAddressFromKey(prefixLength, key);
    }

    public Address(int prefixLength, ECKey key) throws InvalidPrefixLengthException{
        if (prefixLength > 32) throw new InvalidPrefixLengthException();
        this.key = key;
        createAddressFromKey(prefixLength, key);
    }

    public Address(String address) throws AddressFormatException{
        parse(address);
    }

    private void createAddressFromKey(int prefixLength, ECKey key){
        this.prefixLength = prefixLength;
        byte[] length = new byte[]{(byte) prefixLength};
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(length);
            outputStream.write(key.getPubKeyBytes());
        } catch (IOException e){e.printStackTrace();}
        byte[] pubkeyBytes = key.getPubKeyBytes();
        for (int i=0; i<33; i++){
            this.publicKey[i] = pubkeyBytes[i];
        }
        for (int i=0; i<4; i++){
            this.prefix[i] = publicKey[i];
        }
        byte[] addressBytes = outputStream.toByteArray();
        byte[] fullChecksum = Utils.doubleDigest(addressBytes);
        for (int i=0; i<4; i++){this.checksum[i] = fullChecksum[i];}
        byte[] ver = new byte[]{(byte) version};
        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream( );
        try {
            outputStream2.write(ver);
            outputStream2.write(addressBytes);
            outputStream2.write(checksum);
        } catch (IOException e){e.printStackTrace();}
        this.versionedchecksummed = outputStream2.toByteArray();
    }

    private void parse(String address) throws AddressFormatException{
        this.versionedchecksummed = Base58.decode(address);
        this.version = this.versionedchecksummed[0] & 0xFF;
        this.prefixLength = this.versionedchecksummed[1] & 0xFF;
        for (int i=0; i<4; i++){
            this.prefix[i] = this.versionedchecksummed[i+2];
        }
        for (int i=0; i<33; i++){
            this.publicKey[i] = this.versionedchecksummed[i+2];
        }
        for (int i=0; i<4; i++) {
            this.checksum[i] = this.versionedchecksummed[i + 35];
        }
        this.key = ECKey.fromPubOnly(this.publicKey);
    }

    public String toString(){
        return Base58.encode(versionedchecksummed);
    }

    public ECPublicKey getPublicKey() {
        return (ECPublicKey) this.key.getPubKey();
    }

    public ECKey getECKey(){
        return key;
    }

}
