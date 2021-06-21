import org.twostack.bitcoin4j.Address
import org.twostack.bitcoin4j.ECKey
import org.twostack.bitcoin4j.PrivateKey
import org.twostack.bitcoin4j.Utils
import org.twostack.bitcoin4j.params.NetworkAddressType
import org.twostack.bitcoin4j.params.NetworkType
import org.twostack.bitcoin4j.transaction.*
import java.math.BigInteger


/*
alice: cRHYFwjjw2Xn2gjxdGw6RRgKJZqipZx7j8i64NdwzxcD6SezEZV5
bob: cStLVGeWx7fVYKKDXYWVeEbEcPZEC4TD73DjQpHCks2Y8EAjVDSS
 */
fun main(args: Array<String>) {
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
        alice receiving funds txId:  dc7cf11c6b4d5d266186b382536ba46f8953888ef0f6a484cd740abf5c26a683
     */

    val aliceFundingRawTx = "02000000018702cc99cf03a325996548b8a94deb4a8e1b8702c4b4ebcc3430240673e289700000000048473044022078ea7a1fdad2419978fc8bae2c0e5effb33812990ea62f6ff08ab7d79b3db40702203b5e825694b52e5b29f18f7999d352ed0c874e99d5c590ae3dc89596e3e68cbe41feffffff0200ca9a3b000000001976a914f5d33ee198ad13840ce410ba96e149e463a6c35288ac40276bee000000001976a9146cfda1fe89e270186a8e77af607ea0d2593772b188ac65000000"
    val aliceFundingTx : Transaction = Transaction.fromHex(aliceFundingRawTx)

    val unlockBuilder : UnlockingScriptBuilder = P2PKHUnlockBuilder(alicePub)

    val bobLockingBuilder: LockingScriptBuilder = P2PKHLockBuilder(bobAddress)
    val aliceLockingBuilder: LockingScriptBuilder = P2PKHLockBuilder(aliceAddress)


    val txBuilder : TransactionBuilder = TransactionBuilder()
    val spendingTx: Transaction = txBuilder.spendFromTransaction(aliceFundingTx, 0, Transaction.NLOCKTIME_MAX_VALUE, unlockBuilder)
        .spendTo(bobLockingBuilder, BigInteger.valueOf(10000))
        .sendChangeTo(aliceAddress, aliceLockingBuilder)
        .withFeePerKb(512)
        .build(true)

    val fundingOutput: TransactionOutput = aliceFundingTx.outputs[0]
    TransactionSigner().sign(
        spendingTx,
        fundingOutput,
        0,
        alicePrivateKey,
        SigHashType.ALL.value or SigHashType.FORKID.value)

    println(Utils.HEX.encode(spendingTx.serialize()))


}