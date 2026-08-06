package com.example.personalvault.ui.vault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalvault.model.*
import com.example.personalvault.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryFormDialog(
    initialSection: SectionType?,
    editingEntry: VaultEntry?,
    onDismiss: () -> Unit,
    onSave: (VaultEntry) -> Unit,
    onOpenGenerator: () -> Unit
) {
    var selectedSection by remember {
        mutableStateOf(editingEntry?.sectionType ?: initialSection ?: SectionType.PASSWORD)
    }

    // Password Fields
    var passwordTitle by remember { mutableStateOf((editingEntry as? VaultEntry.Password)?.accountName ?: "") }
    var passwordUsername by remember { mutableStateOf((editingEntry as? VaultEntry.Password)?.username ?: "") }
    var passwordValue by remember { mutableStateOf((editingEntry as? VaultEntry.Password)?.passwordValue ?: "") }
    var passwordUrl by remember { mutableStateOf((editingEntry as? VaultEntry.Password)?.url ?: "") }
    var passwordCategory by remember { mutableStateOf((editingEntry as? VaultEntry.Password)?.category ?: "General") }
    var passwordIsFavorite by remember { mutableStateOf((editingEntry as? VaultEntry.Password)?.isFavorite ?: false) }
    var passwordNotes by remember { mutableStateOf((editingEntry as? VaultEntry.Password)?.additionalInfo ?: "") }
    var showPasswordVal by remember { mutableStateOf(false) }

    // Document Fields
    var docName by remember { mutableStateOf((editingEntry as? VaultEntry.Document)?.documentName ?: "") }
    var docType by remember { mutableStateOf((editingEntry as? VaultEntry.Document)?.documentType ?: DocumentType.OTHER) }
    var docDesc by remember { mutableStateOf((editingEntry as? VaultEntry.Document)?.description ?: "") }
    var docTags by remember { mutableStateOf((editingEntry as? VaultEntry.Document)?.tags?.joinToString(", ") ?: "") }

    // Driving License Fields
    var licName by remember { mutableStateOf((editingEntry as? VaultEntry.DrivingLicense)?.licenseName ?: "") }
    var licHolder by remember { mutableStateOf((editingEntry as? VaultEntry.DrivingLicense)?.holderName ?: "") }
    var licNumber by remember { mutableStateOf((editingEntry as? VaultEntry.DrivingLicense)?.licenseNumber ?: "") }
    var licType by remember { mutableStateOf((editingEntry as? VaultEntry.DrivingLicense)?.licenseType ?: LicenseType.NON_PROFESSIONAL) }
    var licIssue by remember { mutableStateOf((editingEntry as? VaultEntry.DrivingLicense)?.issueDate ?: "") }
    var licExpiry by remember { mutableStateOf((editingEntry as? VaultEntry.DrivingLicense)?.expiryDate ?: "") }

    // Certificate Fields
    var certName by remember { mutableStateOf((editingEntry as? VaultEntry.Certificate)?.certificateName ?: "") }
    var certType by remember { mutableStateOf((editingEntry as? VaultEntry.Certificate)?.certificateType ?: CertificateType.OTHER) }
    var certInst by remember { mutableStateOf((editingEntry as? VaultEntry.Certificate)?.institutionName ?: "") }
    var certYear by remember { mutableStateOf((editingEntry as? VaultEntry.Certificate)?.yearOfCompletion ?: "") }
    var certDesc by remember { mutableStateOf((editingEntry as? VaultEntry.Certificate)?.description ?: "") }

    // ID Card Fields
    var idName by remember { mutableStateOf((editingEntry as? VaultEntry.IdCard)?.cardName ?: "") }
    var idType by remember { mutableStateOf((editingEntry as? VaultEntry.IdCard)?.cardType ?: IdCardType.NATIONAL_ID) }
    var idNumber by remember { mutableStateOf((editingEntry as? VaultEntry.IdCard)?.cardNumber ?: "") }
    var idHolder by remember { mutableStateOf((editingEntry as? VaultEntry.IdCard)?.holderName ?: "") }
    var idIssue by remember { mutableStateOf((editingEntry as? VaultEntry.IdCard)?.issueDate ?: "") }
    var idExpiry by remember { mutableStateOf((editingEntry as? VaultEntry.IdCard)?.expiryDate ?: "") }
    var idDesc by remember { mutableStateOf((editingEntry as? VaultEntry.IdCard)?.description ?: "") }

    // Bank Fields
    var bankName by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.bankName ?: "") }
    var bankType by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.bankType ?: BankAccountType.SAVINGS) }
    var bankBranch by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.branchName ?: "") }
    var bankAccountNum by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.accountNumber ?: "") }
    var bankHolder by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.accountHolderName ?: "") }
    var bankRouting by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.routingNumber ?: "") }
    var bankSwift by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.swiftCode ?: "") }
    var bankNotes by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.additionalInfo ?: "") }

    // Bank Card sub-entry
    var cardNum by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.cards?.firstOrNull()?.cardNumber ?: "") }
    var cardHolder by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.cards?.firstOrNull()?.cardHolderName ?: "") }
    var cardExpiry by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.cards?.firstOrNull()?.expiryDate ?: "") }
    var cardCvv by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.cards?.firstOrNull()?.cvv ?: "") }
    var cardPin by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.cards?.firstOrNull()?.pin ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VaultBgSurface,
        title = {
            Text(
                text = if (editingEntry != null) "Edit Entry" else "New Vault Entry",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (editingEntry == null) {
                    // Category selector chips
                    Text("Select Category", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectionType.values().forEach { sec ->
                            FilterChip(
                                selected = selectedSection == sec,
                                onClick = { selectedSection = sec },
                                label = { Text(sec.label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentPrimary,
                                    selectedLabelColor = TextPrimary
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when (selectedSection) {
                    SectionType.PASSWORD -> {
                        OutlinedTextField(
                            value = passwordTitle,
                            onValueChange = { passwordTitle = it },
                            label = { Text("Account Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordUsername,
                            onValueChange = { passwordUsername = it },
                            label = { Text("Username / Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = passwordValue,
                                onValueChange = { passwordValue = it },
                                label = { Text("Password") },
                                singleLine = true,
                                visualTransformation = if (showPasswordVal) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showPasswordVal = !showPasswordVal }) {
                                        Icon(
                                            imageVector = if (showPasswordVal) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle"
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            IconButton(onClick = onOpenGenerator) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Generator",
                                    tint = AccentPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordUrl,
                            onValueChange = { passwordUrl = it },
                            label = { Text("Website / App URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = passwordIsFavorite,
                                onCheckedChange = { passwordIsFavorite = it }
                            )
                            Text("Mark as Favorite", color = TextPrimary, fontSize = 13.sp)
                        }
                    }

                    SectionType.DOCUMENT -> {
                        OutlinedTextField(
                            value = docName,
                            onValueChange = { docName = it },
                            label = { Text("Document Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = docDesc,
                            onValueChange = { docDesc = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = docTags,
                            onValueChange = { docTags = it },
                            label = { Text("Tags (comma separated)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    SectionType.DRIVING_LICENSE -> {
                        OutlinedTextField(
                            value = licName,
                            onValueChange = { licName = it },
                            label = { Text("License Title *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = licHolder,
                            onValueChange = { licHolder = it },
                            label = { Text("Holder Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = licNumber,
                            onValueChange = { licNumber = it },
                            label = { Text("License Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = licIssue,
                                onValueChange = { licIssue = it },
                                label = { Text("Issue Date") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = licExpiry,
                                onValueChange = { licExpiry = it },
                                label = { Text("Expiry Date") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    SectionType.CERTIFICATE -> {
                        OutlinedTextField(
                            value = certName,
                            onValueChange = { certName = it },
                            label = { Text("Certificate Title *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = certInst,
                            onValueChange = { certInst = it },
                            label = { Text("Institution / Organization") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = certYear,
                            onValueChange = { certYear = it },
                            label = { Text("Year of Completion") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    SectionType.ID_CARD -> {
                        OutlinedTextField(
                            value = idName,
                            onValueChange = { idName = it },
                            label = { Text("ID Card Title *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = idNumber,
                            onValueChange = { idNumber = it },
                            label = { Text("Card / Document Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = idHolder,
                            onValueChange = { idHolder = it },
                            label = { Text("Holder Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    SectionType.BANK -> {
                        OutlinedTextField(
                            value = bankName,
                            onValueChange = { bankName = it },
                            label = { Text("Bank Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = bankHolder,
                            onValueChange = { bankHolder = it },
                            label = { Text("Account Holder Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = bankAccountNum,
                            onValueChange = { bankAccountNum = it },
                            label = { Text("Account Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cardNum,
                            onValueChange = { cardNum = it },
                            label = { Text("Card Number (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = cardExpiry,
                                onValueChange = { cardExpiry = it },
                                label = { Text("Expiry") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = cardCvv,
                                onValueChange = { cardCvv = it },
                                label = { Text("CVV") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entryToSave: VaultEntry? = when (selectedSection) {
                        SectionType.PASSWORD -> {
                            if (passwordTitle.isBlank()) null
                            else VaultEntry.Password(
                                id = editingEntry?.id ?: java.util.UUID.randomUUID().toString(),
                                createdAt = editingEntry?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                accountName = passwordTitle,
                                username = passwordUsername,
                                passwordValue = passwordValue,
                                url = passwordUrl,
                                category = passwordCategory,
                                isFavorite = passwordIsFavorite,
                                additionalInfo = passwordNotes
                            )
                        }
                        SectionType.DOCUMENT -> {
                            if (docName.isBlank()) null
                            else VaultEntry.Document(
                                id = editingEntry?.id ?: java.util.UUID.randomUUID().toString(),
                                createdAt = editingEntry?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                documentName = docName,
                                documentType = docType,
                                description = docDesc,
                                tags = docTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            )
                        }
                        SectionType.DRIVING_LICENSE -> {
                            if (licName.isBlank()) null
                            else VaultEntry.DrivingLicense(
                                id = editingEntry?.id ?: java.util.UUID.randomUUID().toString(),
                                createdAt = editingEntry?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                licenseName = licName,
                                holderName = licHolder,
                                licenseNumber = licNumber,
                                licenseType = licType,
                                issueDate = licIssue,
                                expiryDate = licExpiry
                            )
                        }
                        SectionType.CERTIFICATE -> {
                            if (certName.isBlank()) null
                            else VaultEntry.Certificate(
                                id = editingEntry?.id ?: java.util.UUID.randomUUID().toString(),
                                createdAt = editingEntry?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                certificateName = certName,
                                certificateType = certType,
                                institutionName = certInst,
                                yearOfCompletion = certYear,
                                description = certDesc
                            )
                        }
                        SectionType.ID_CARD -> {
                            if (idName.isBlank()) null
                            else VaultEntry.IdCard(
                                id = editingEntry?.id ?: java.util.UUID.randomUUID().toString(),
                                createdAt = editingEntry?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                cardName = idName,
                                cardType = idType,
                                cardNumber = idNumber,
                                holderName = idHolder,
                                issueDate = idIssue,
                                expiryDate = idExpiry,
                                description = idDesc
                            )
                        }
                        SectionType.BANK -> {
                            if (bankName.isBlank()) null
                            else {
                                val cardsList = if (cardNum.isNotBlank()) listOf(
                                    BankCard(
                                        cardNumber = cardNum,
                                        cardHolderName = cardHolder,
                                        expiryDate = cardExpiry,
                                        cvv = cardCvv,
                                        pin = cardPin
                                    )
                                ) else emptyList()
                                VaultEntry.Bank(
                                    id = editingEntry?.id ?: java.util.UUID.randomUUID().toString(),
                                    createdAt = editingEntry?.createdAt ?: System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis(),
                                    bankName = bankName,
                                    bankType = bankType,
                                    branchName = bankBranch,
                                    accountNumber = bankAccountNum,
                                    accountHolderName = bankHolder,
                                    routingNumber = bankRouting,
                                    swiftCode = bankSwift,
                                    additionalInfo = bankNotes,
                                    cards = cardsList
                                )
                            }
                        }
                    }

                    if (entryToSave != null) {
                        onSave(entryToSave)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
