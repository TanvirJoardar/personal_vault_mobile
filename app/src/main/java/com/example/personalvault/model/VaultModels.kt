package com.example.personalvault.model

import kotlinx.serialization.Serializable

enum class SectionType(val key: String, val label: String) {
    PASSWORD("password", "Passwords"),
    DOCUMENT("document", "Documents"),
    DRIVING_LICENSE("driving-license", "Driving Licenses"),
    CERTIFICATE("certificate", "Certificates"),
    ID_CARD("id-card", "ID Cards"),
    BANK("bank", "Bank Accounts & Cards")
}

enum class AppScreen {
    SETUP,
    LOCK,
    RECOVERY,
    MAIN
}

@Serializable
data class EncryptedFileAttachment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val encryptedData: String, // base64
    val iv: String             // base64
)

@Serializable
enum class SignInProvider {
    NONE, GOOGLE, MICROSOFT, FACEBOOK, APPLE
}

@Serializable
enum class DocumentType {
    AGREEMENT, INVOICE, RECEIPT, CONTRACT, LEGAL, MEDICAL, TAX, INSURANCE, OTHER
}

@Serializable
enum class LicenseType {
    PROFESSIONAL, NON_PROFESSIONAL, INTERNATIONAL, LEARNER, OTHER
}

@Serializable
enum class CertificateType {
    SSC, HSC, UNIVERSITY, MASTERS, PHD, PROFESSIONAL, DIPLOMA, OTHER
}

@Serializable
enum class IdCardType {
    NATIONAL_ID, SCHOOL_ID, COLLEGE_ID, UNIVERSITY_ID, JOB_ID, VOTER_ID, PASSPORT, OTHER
}

@Serializable
enum class BankAccountType {
    SAVINGS, CURRENT, FIXED_DEPOSIT, DPS, LOAN, OTHER
}

@Serializable
enum class BankCardType {
    VISA, MASTERCARD, DEBIT, CREDIT, PREPAID, OTHER
}

@Serializable
data class BankCard(
    val id: String = java.util.UUID.randomUUID().toString(),
    val cardType: BankCardType = BankCardType.DEBIT,
    val cardNumber: String = "",
    val cardHolderName: String = "",
    val expiryDate: String = "",
    val pin: String = "",
    val cvv: String = "",
    val cardImage: EncryptedFileAttachment? = null
)

@Serializable
sealed class VaultEntry {
    abstract val id: String
    abstract val createdAt: Long
    abstract val updatedAt: Long
    abstract val sectionType: SectionType
    abstract val title: String

    @Serializable
    data class Password(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        val accountName: String,
        val username: String = "",
        val passwordValue: String = "",
        val url: String = "",
        val additionalInfo: String = "",
        val signInProvider: SignInProvider = SignInProvider.NONE,
        val category: String = "General",
        val isFavorite: Boolean = false,
        val attachments: List<EncryptedFileAttachment> = emptyList()
    ) : VaultEntry() {
        override val sectionType: SectionType get() = SectionType.PASSWORD
        override val title: String get() = accountName
    }

    @Serializable
    data class Document(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        val documentName: String,
        val documentType: DocumentType = DocumentType.OTHER,
        val description: String = "",
        val tags: List<String> = emptyList(),
        val files: List<EncryptedFileAttachment> = emptyList()
    ) : VaultEntry() {
        override val sectionType: SectionType get() = SectionType.DOCUMENT
        override val title: String get() = documentName
    }

    @Serializable
    data class DrivingLicense(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        val licenseName: String,
        val holderName: String = "",
        val licenseNumber: String = "",
        val licenseType: LicenseType = LicenseType.NON_PROFESSIONAL,
        val issueDate: String = "",
        val expiryDate: String = "",
        val files: List<EncryptedFileAttachment> = emptyList()
    ) : VaultEntry() {
        override val sectionType: SectionType get() = SectionType.DRIVING_LICENSE
        override val title: String get() = licenseName
    }

    @Serializable
    data class Certificate(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        val certificateName: String,
        val certificateType: CertificateType = CertificateType.OTHER,
        val institutionName: String = "",
        val description: String = "",
        val yearOfCompletion: String = "",
        val files: List<EncryptedFileAttachment> = emptyList()
    ) : VaultEntry() {
        override val sectionType: SectionType get() = SectionType.CERTIFICATE
        override val title: String get() = certificateName
    }

    @Serializable
    data class IdCard(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        val cardName: String,
        val cardType: IdCardType = IdCardType.NATIONAL_ID,
        val cardNumber: String = "",
        val holderName: String = "",
        val description: String = "",
        val issueDate: String = "",
        val expiryDate: String = "",
        val files: List<EncryptedFileAttachment> = emptyList()
    ) : VaultEntry() {
        override val sectionType: SectionType get() = SectionType.ID_CARD
        override val title: String get() = cardName
    }

    @Serializable
    data class Bank(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        val bankName: String,
        val bankType: BankAccountType = BankAccountType.SAVINGS,
        val branchName: String = "",
        val accountNumber: String = "",
        val accountHolderName: String = "",
        val routingNumber: String = "",
        val swiftCode: String = "",
        val ifscCode: String = "",
        val mobileNumber: String = "",
        val email: String = "",
        val additionalInfo: String = "",
        val signatureFile: EncryptedFileAttachment? = null,
        val cards: List<BankCard> = emptyList()
    ) : VaultEntry() {
        override val sectionType: SectionType get() = SectionType.BANK
        override val title: String get() = bankName
    }
}

@Serializable
data class VaultExportData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val entries: List<EncryptedEntryExport>
)

@Serializable
data class EncryptedEntryExport(
    val id: String,
    val sectionType: String,
    val ciphertext: String,
    val iv: String,
    val createdAt: Long,
    val updatedAt: Long
)
