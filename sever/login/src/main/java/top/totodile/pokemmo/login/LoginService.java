package top.totodile.pokemmo.login;

import top.totodile.pokemmo.protocol.LoginRequest;
import top.totodile.pokemmo.protocol.LoginStatus;

import java.util.HexFormat;

/** 未找到账号和密码不符统一返回凭据错误。 */
public final class LoginService {
    private final AccountRepository accounts;
    private final java.security.PrivateKey credentialKey;

    public LoginService(AccountRepository accounts) {
        this(accounts, null);
    }

    public LoginService(AccountRepository accounts, java.security.PrivateKey credentialKey) {
        this.accounts = accounts;
        this.credentialKey = credentialKey;
    }

    public LoginStatus authenticate(LoginRequest request) {
        if (request.credential().length > 0) {
            var expected = accounts.passwordDigest(request.username());
            return expected.isPresent() && credentialKey != null && SavedLoginCredential.verify(credentialKey,
                    request.username(), expected.get(), request.credential())
                    ? LoginStatus.AUTHENTICATED : LoginStatus.INVALID_PASSWORD;
        }
        byte[] supplied = HexFormat.of().parseHex(request.passwordDigest());
        return accounts.verifyPassword(request.username(), supplied)
                ? LoginStatus.AUTHENTICATED : LoginStatus.INVALID_PASSWORD;
    }

    public String issueCredential(LoginRequest request) {
        if (!request.remember() || credentialKey == null) return "";
        return java.util.Base64.getEncoder().encodeToString(SavedLoginCredential.issue(credentialKey,
                request.username(), accounts.passwordDigest(request.username()).orElseThrow()));
    }
}
