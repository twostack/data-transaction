import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import org.twostack.bitcoin4j.Address
import org.twostack.bitcoin4j.ECKey
import org.twostack.bitcoin4j.PrivateKey
import org.twostack.bitcoin4j.Utils
import org.twostack.bitcoin4j.Utils.WHITESPACE_SPLITTER
import org.twostack.bitcoin4j.crypto.DeterministicKey
import org.twostack.bitcoin4j.crypto.HDKeyDerivation
import org.twostack.bitcoin4j.crypto.MnemonicCode
import org.twostack.bitcoin4j.params.NetworkAddressType
import org.twostack.bitcoin4j.params.NetworkType
import org.twostack.bitcoin4j.transaction.*
import java.math.BigInteger
import java.nio.ByteBuffer

data class WocUtxo( val height: Int, val tx_pos: Int, val tx_hash: String, val value: Int)


fun getUtxos(address: String): List<WocUtxo> {

     /*
     curl https://api.whatsonchain.com/v1/bsv/test/address/mp1FvvDPebFso9RGivAkKsEmeduDLiwNF1/unspent
      */

    val urlStr =  "https://api.whatsonchain.com/v1/bsv/test/address/${address}/unspent"

    val (request, response, result) = urlStr.httpGet().responseObject<List<WocUtxo>>()

    when (result) {
        is Result.Failure -> {
            val ex = result.getException()
            println(ex)
        }
        is Result.Success -> {
            return result.get()
        }
    }

    return ArrayList()
}

/*
fun sendRawTransaction(tx: Transaction): String {

}

fun getRawTransaction(txId: String): String {

}

 */

fun fromSeed(seedPhrase: String): DeterministicKey {
    val words: List<String> = WHITESPACE_SPLITTER.splitToList(seedPhrase);
    val mc: MnemonicCode = MnemonicCode();
    mc.check(words);
    val seedBytes: ByteArray = MnemonicCode.toSeed(words, "");

    val dk: DeterministicKey = HDKeyDerivation.createMasterPrivateKey(seedBytes);

    return dk;
}

fun fromAliceToBobRegtest() {

//    val aliceP1: PrivateKey  = PrivateKey(ECKey())
//    aliceP1.toWif(NetworkType.TEST)
//    val bobPrivateKey: PrivateKey = PrivateKey(ECKey())

    val aliceWif = "cRHYFwjjw2Xn2gjxdGw6RRgKJZqipZx7j8i64NdwzxcD6SezEZV5"
    val alicePrivateKey: PrivateKey = PrivateKey.fromWIF(aliceWif)

    val bobWif = "cStLVGeWx7fVYKKDXYWVeEbEcPZEC4TD73DjQpHCks2Y8EAjVDSS"
    val bobPrivateKey: PrivateKey = PrivateKey.fromWIF(bobWif)

    //Get the public Keys
    val alicePub = alicePrivateKey.publicKey
    val bobPub = bobPrivateKey.publicKey

    //Get the addresses
    val aliceAddress = Address.fromKey(NetworkAddressType.TEST_PKH, alicePub)
    val bobAddress = Address.fromKey(NetworkAddressType.TEST_PKH, bobPub)

    println(aliceAddress)
    println(bobAddress)

    val aliceAddrStr = "n3vkuf1YPY3QRXx3kaLF6p8QhgWDZ2zg8F"
    val bobAddrStr = "mpjFGX8CRr57qaGZKibryf1VqSwGQL5Khp"

    /*
        alice receiving funds txId:  5410d12b14d42ab2b343a6c62cbaf6b65f6c0b29d8bb544dfe0edd7260ac449e
     */

    val aliceFundingRawTx =
        "0200000005a5b017920bbcb838e09c97b6cd78ec61000b42bb97d9f929aca85c0a3b1b5a790000000048473044022054327452d90ba3f490a7b11de9738ce74b3a78dcee4ffb1d88c165db2cbe817c022056b51620f7c747e2b6280116dbe99d125e2e8c87ebaffc7da40292151b43c74841feffffff9952aca573743769e8d711e8a69ae1a93af758bdfa5d9fa8a55c9f7171e4d8d700000000484730440220763cbd15e8ddf2bd05913e3b2d21976dc7f81aeb5f49321d00afdd0ae516da8e02202b894a93da80ad5254389b89ac720766913feee638bf217731d487c406e8da4d41fefffffff676bf4e831f2efe440d5d651f64dca9ad439fb7978b20d5298c10f90a96770600000000484730440220501dfe0c96cb6c496e850ef01b6e39368307ada5d33b6acddf1c492170877b2e02206ea71298ceb91cd3c8927300e6a18cb157d5bb19f0c7f9f6f1ee669f27b867af41feffffffc2590bf1b6e37ea15e1ca7e749f92a9689a7f201b137996241966d1f0ec323ee000000004847304402204c6fd3087fe2acda12abea8fe6fb0df0af15f61a96f1e9710ee72f956db167740220288d69a26cbfb196f330a4f1121e93d4edacca33db844274d170393725439ca541feffffffe58e8d2f2bdfc470e911e3cfe57ea70c30db566160c0ff70383c7b017df7e0360000000049483045022100a713897e64d531256c898eb067f9343bcae22dda2682b2aa0f8c5f2fe9878640022030c9cf32a83c0bab5b5f8f7bd1f9427b57aea4e79088b3ed71ad121431f00fab41feffffff02a7655900000000001976a914e8a871b00daf16ba764bf5808890772ef53831aa88ac00ca9a3b000000001976a914f5d33ee198ad13840ce410ba96e149e463a6c35288acaa050000"
    val aliceFundingTx: Transaction = Transaction.fromHex(aliceFundingRawTx)

    val unlockBuilder: UnlockingScriptBuilder = P2PKHUnlockBuilder(alicePub)

//    val bobLockingBuilder: LockingScriptBuilder = P2PKHLockBuilder(bobAddress)
    val bobLockingBuilder: LockingScriptBuilder = P2PKHDataLockBuilder(bobAddress, ByteBuffer.wrap("Hello, World!".encodeToByteArray()))

    val aliceLockingBuilder: LockingScriptBuilder = P2PKHLockBuilder(aliceAddress)

    val txBuilder: TransactionBuilder = TransactionBuilder()
    val spendingTx: Transaction = txBuilder.spendFromTransaction(aliceFundingTx, 1, Transaction.NLOCKTIME_MAX_VALUE, unlockBuilder)
        .spendTo(bobLockingBuilder, BigInteger.valueOf(10000))
        .sendChangeTo(aliceAddress, aliceLockingBuilder)
        .withFeePerKb(512)
        .build(true)

    val fundingOutput: TransactionOutput = aliceFundingTx.outputs[1]
    TransactionSigner().sign(
        spendingTx,
        fundingOutput,
        0,
        alicePrivateKey,
        SigHashType.ALL.value or SigHashType.FORKID.value
    )

    println(Utils.HEX.encode(spendingTx.serialize()))

}

/*
alice: cRHYFwjjw2Xn2gjxdGw6RRgKJZqipZx7j8i64NdwzxcD6SezEZV5
bob: cStLVGeWx7fVYKKDXYWVeEbEcPZEC4TD73DjQpHCks2Y8EAjVDSS
 */
fun main(args: Array<String>) {

    val utxos = getUtxos("mp1FvvDPebFso9RGivAkKsEmeduDLiwNF1")
    /*
    val seed = "edge eagle blue panda zone tiger emerge trial limit royal average basket"
    val dk: DeterministicKey = fromSeed(seed)

    val pk: PrivateKey = PrivateKey(ECKey.fromPrivate(dk.privKey));

    val address = Address.fromKey(NetworkAddressType.TEST_PKH, pk.publicKey)

    println(address)
     */

}