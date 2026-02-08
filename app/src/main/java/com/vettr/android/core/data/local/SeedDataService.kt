package com.vettr.android.core.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.vettr.android.core.model.Executive
import com.vettr.android.core.model.Filing
import com.vettr.android.core.model.Stock
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private val Context.dataStore by preferencesDataStore(name = "seed_prefs")

@Singleton
class SeedDataService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stockDao: StockDao,
    private val filingDao: FilingDao,
    private val executiveDao: ExecutiveDao
) {
    private val SEED_COMPLETE_KEY = booleanPreferencesKey("seed_complete")

    /**
     * Populates database with pilot seed data.
     */
    suspend fun seedAllData() {
        // Seed stocks (US-058)
        val stocks = seedStocks()

        // Seed filings (US-059)
        seedFilings(stocks)

        // Seed executives (US-059)
        seedExecutives(stocks)
    }

    private suspend fun seedStocks(): List<Stock> {
        val stocks = listOf(
            // Strong Buy stocks (score 80-95)
            Stock(
                ticker = "BTO.TO",
                name = "B2Gold Corp",
                exchange = "TSX",
                sector = "Mining",
                marketCap = 5_400_000_000.0,
                price = 4.85,
                priceChange = 0.12,
                vetrScore = 88,
                isFavorite = false
            ),
            Stock(
                ticker = "TVE.TO",
                name = "Tamarack Valley Energy",
                exchange = "TSX",
                sector = "Energy",
                marketCap = 2_800_000_000.0,
                price = 4.23,
                priceChange = 0.18,
                vetrScore = 85,
                isFavorite = false
            ),
            Stock(
                ticker = "LSPD.TO",
                name = "Lightspeed Commerce",
                exchange = "TSX",
                sector = "Technology",
                marketCap = 3_200_000_000.0,
                price = 21.45,
                priceChange = -0.35,
                vetrScore = 82,
                isFavorite = false
            ),
            Stock(
                ticker = "IE.TO",
                name = "Ivanhoe Electric",
                exchange = "TSX",
                sector = "Mining",
                marketCap = 1_500_000_000.0,
                price = 12.80,
                priceChange = 0.45,
                vetrScore = 90,
                isFavorite = false
            ),
            Stock(
                ticker = "CJ.TO",
                name = "Cardinal Energy",
                exchange = "TSX",
                sector = "Energy",
                marketCap = 1_100_000_000.0,
                price = 7.15,
                priceChange = 0.22,
                vetrScore = 83,
                isFavorite = false
            ),

            // Buy stocks (score 60-79)
            Stock(
                ticker = "DML.TO",
                name = "Denison Mines",
                exchange = "TSX",
                sector = "Mining",
                marketCap = 1_800_000_000.0,
                price = 2.35,
                priceChange = -0.08,
                vetrScore = 75,
                isFavorite = false
            ),
            Stock(
                ticker = "LAAC.TO",
                name = "Lithium Americas (Argentina)",
                exchange = "TSX",
                sector = "Mining",
                marketCap = 900_000_000.0,
                price = 4.60,
                priceChange = 0.15,
                vetrScore = 72,
                isFavorite = false
            ),
            Stock(
                ticker = "GRN.TO",
                name = "Greenlane Renewables",
                exchange = "TSX",
                sector = "Energy",
                marketCap = 180_000_000.0,
                price = 1.12,
                priceChange = -0.03,
                vetrScore = 68,
                isFavorite = false
            ),
            Stock(
                ticker = "NPI.TO",
                name = "Northland Power",
                exchange = "TSX",
                sector = "Energy",
                marketCap = 4_200_000_000.0,
                price = 18.75,
                priceChange = 0.28,
                vetrScore = 70,
                isFavorite = false
            ),
            Stock(
                ticker = "DCBO.TO",
                name = "Docebo Inc",
                exchange = "TSX",
                sector = "Technology",
                marketCap = 1_600_000_000.0,
                price = 52.30,
                priceChange = -0.95,
                vetrScore = 73,
                isFavorite = false
            ),
            Stock(
                ticker = "DND.TO",
                name = "Dye & Durham",
                exchange = "TSX",
                sector = "Technology",
                marketCap = 1_400_000_000.0,
                price = 19.85,
                priceChange = 0.42,
                vetrScore = 67,
                isFavorite = false
            ),
            Stock(
                ticker = "ENGH.TO",
                name = "Enghouse Systems",
                exchange = "TSX",
                sector = "Technology",
                marketCap = 1_900_000_000.0,
                price = 34.20,
                priceChange = -0.18,
                vetrScore = 71,
                isFavorite = false
            ),
            Stock(
                ticker = "PAID.CSE",
                name = "PAID Inc",
                exchange = "CSE",
                sector = "Technology",
                marketCap = 45_000_000.0,
                price = 0.65,
                priceChange = 0.05,
                vetrScore = 65,
                isFavorite = false
            ),
            Stock(
                ticker = "FOM.V",
                name = "Foran Mining",
                exchange = "TSXV",
                sector = "Mining",
                marketCap = 620_000_000.0,
                price = 3.15,
                priceChange = -0.12,
                vetrScore = 62,
                isFavorite = false
            ),

            // Hold stocks (score 40-59)
            Stock(
                ticker = "AMC.V",
                name = "Arizona Metals",
                exchange = "TSXV",
                sector = "Mining",
                marketCap = 280_000_000.0,
                price = 2.88,
                priceChange = 0.08,
                vetrScore = 52,
                isFavorite = false
            ),
            Stock(
                ticker = "GMIN.TO",
                name = "G Mining Ventures",
                exchange = "TSX",
                sector = "Mining",
                marketCap = 350_000_000.0,
                price = 2.45,
                priceChange = -0.05,
                vetrScore = 48,
                isFavorite = false
            ),
            Stock(
                ticker = "SOIL.V",
                name = "SOiL Innovations",
                exchange = "TSXV",
                sector = "Agriculture",
                marketCap = 25_000_000.0,
                price = 0.38,
                priceChange = 0.02,
                vetrScore = 50,
                isFavorite = false
            ),
            Stock(
                ticker = "QUIS.V",
                name = "Quisitive Technology Solutions",
                exchange = "TSXV",
                sector = "Technology",
                marketCap = 95_000_000.0,
                price = 1.42,
                priceChange = -0.07,
                vetrScore = 53,
                isFavorite = false
            ),
            Stock(
                ticker = "PNG.V",
                name = "PNG Energy",
                exchange = "TSXV",
                sector = "Energy",
                marketCap = 12_000_000.0,
                price = 0.15,
                priceChange = 0.01,
                vetrScore = 45,
                isFavorite = false
            ),
            Stock(
                ticker = "PLUR.V",
                name = "Plurilock Security",
                exchange = "TSXV",
                sector = "Technology",
                marketCap = 32_000_000.0,
                price = 0.52,
                priceChange = -0.03,
                vetrScore = 49,
                isFavorite = false
            ),
            Stock(
                ticker = "NDA.V",
                name = "New Destiny Mining",
                exchange = "TSXV",
                sector = "Mining",
                marketCap = 18_000_000.0,
                price = 0.22,
                priceChange = 0.00,
                vetrScore = 47,
                isFavorite = false
            ),

            // Caution stocks (score 30-39)
            Stock(
                ticker = "ACT.CSE",
                name = "Applied Blockchain",
                exchange = "CSE",
                sector = "Technology",
                marketCap = 8_000_000.0,
                price = 0.08,
                priceChange = -0.02,
                vetrScore = 35,
                isFavorite = false
            ),
            Stock(
                ticker = "NTAR.CSE",
                name = "Nextech AR Solutions",
                exchange = "CSE",
                sector = "Technology",
                marketCap = 22_000_000.0,
                price = 0.28,
                priceChange = -0.04,
                vetrScore = 38,
                isFavorite = false
            ),

            // Additional stocks to reach 25
            Stock(
                ticker = "BIR.TO",
                name = "Birchcliff Energy",
                exchange = "TSX",
                sector = "Energy",
                marketCap = 1_100_000_000.0,
                price = 4.15,
                priceChange = 0.18,
                vetrScore = 33,
                isFavorite = false
            ),
            Stock(
                ticker = "VET.TO",
                name = "Vermilion Energy",
                exchange = "TSX",
                sector = "Energy",
                marketCap = 1_800_000_000.0,
                price = 11.20,
                priceChange = -0.25,
                vetrScore = 37,
                isFavorite = false
            )
        )

        stockDao.insertAll(stocks)
        return stocks
    }

    private suspend fun seedFilings(stocks: List<Stock>) {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val filings = mutableListOf<Filing>()

        stocks.forEach { stock ->
            // Add 3-5 filings per stock with dates in the last 6 months
            val numFilings = (3..5).random()

            for (i in 0 until numFilings) {
                val daysAgo = (1..180).random()
                val filingDate = now - (daysAgo * oneDayMillis)

                val filing = when (i % 3) {
                    0 -> Filing(
                        stockId = stock.id,
                        type = "MD&A",
                        title = "Management Discussion & Analysis - Q${((daysAgo / 90) % 4) + 1} ${if (daysAgo < 90) "2026" else "2025"}",
                        date = filingDate,
                        summary = "Management discusses operational results, market conditions, and strategic initiatives for ${stock.name}. Financial performance metrics and future outlook are reviewed.",
                        isRead = false,
                        isMaterial = true
                    )
                    1 -> Filing(
                        stockId = stock.id,
                        type = "Press Release",
                        title = "${stock.name} ${getPressReleaseTitle(stock.sector)}",
                        date = filingDate,
                        summary = "Company announces ${getPressReleaseSummary(stock.sector)} with significant implications for stakeholders and market positioning.",
                        isRead = false,
                        isMaterial = (i % 2 == 0)
                    )
                    else -> Filing(
                        stockId = stock.id,
                        type = "Financial Statements",
                        title = "Quarterly Financial Statements - Q${((daysAgo / 90) % 4) + 1}",
                        date = filingDate,
                        summary = "Complete financial statements including balance sheet, income statement, cash flow statement, and notes. ${stock.name} reports ${if (stock.priceChange >= 0) "positive" else "mixed"} financial results.",
                        isRead = (i % 3 == 0),
                        isMaterial = true
                    )
                }
                filings.add(filing)
            }
        }

        filingDao.insertAll(filings)
    }

    private fun getPressReleaseTitle(sector: String): String {
        return when (sector) {
            "Mining" -> listOf(
                "Announces High-Grade Drill Results",
                "Completes Strategic Acquisition",
                "Secures $50M Financing Package",
                "Updates Mineral Resource Estimate"
            ).random()
            "Energy" -> listOf(
                "Reports Record Production Levels",
                "Announces Strategic Partnership",
                "Increases Quarterly Dividend",
                "Expands Operations to New Region"
            ).random()
            "Technology" -> listOf(
                "Launches New Product Suite",
                "Reports Strong User Growth",
                "Partners with Major Enterprise Client",
                "Expands International Operations"
            ).random()
            else -> listOf(
                "Provides Business Update",
                "Announces Strategic Initiative",
                "Reports Operational Results"
            ).random()
        }
    }

    private fun getPressReleaseSummary(sector: String): String {
        return when (sector) {
            "Mining" -> listOf(
                "promising drill results from exploration program",
                "completion of strategic asset acquisition",
                "successful equity financing round",
                "updated resource estimates showing growth"
            ).random()
            "Energy" -> listOf(
                "increased production from key assets",
                "new strategic partnership agreement",
                "enhanced shareholder returns",
                "expansion into new markets"
            ).random()
            "Technology" -> listOf(
                "new product innovation and features",
                "accelerated customer acquisition",
                "major enterprise partnership",
                "geographic market expansion"
            ).random()
            else -> listOf(
                "important business developments",
                "strategic operational updates",
                "quarterly business highlights"
            ).random()
        }
    }

    private suspend fun seedExecutives(stocks: List<Stock>) {
        val gson = Gson()
        val executives = mutableListOf<Executive>()

        stocks.forEach { stock ->
            // Add 2-4 executives per stock
            val numExecutives = (2..4).random()

            for (i in 0 until numExecutives) {
                val executive = when (i) {
                    0 -> Executive(
                        stockId = stock.id,
                        name = getExecutiveName(),
                        title = "Chief Executive Officer",
                        yearsAtCompany = Random.nextDouble(3.0, 15.0),
                        previousCompanies = gson.toJson(getPreviousCompanies(stock.sector)),
                        education = getEducation(),
                        specialization = getSpecialization(stock.sector, "CEO"),
                        socialLinkedIn = null,
                        socialTwitter = null
                    )
                    1 -> Executive(
                        stockId = stock.id,
                        name = getExecutiveName(),
                        title = "Chief Financial Officer",
                        yearsAtCompany = Random.nextDouble(2.0, 12.0),
                        previousCompanies = gson.toJson(getPreviousCompanies(stock.sector)),
                        education = getEducation("Finance"),
                        specialization = getSpecialization(stock.sector, "CFO"),
                        socialLinkedIn = null,
                        socialTwitter = null
                    )
                    2 -> Executive(
                        stockId = stock.id,
                        name = getExecutiveName(),
                        title = if (stock.sector == "Mining") "VP Exploration" else if (stock.sector == "Technology") "Chief Technology Officer" else "VP Operations",
                        yearsAtCompany = Random.nextDouble(1.5, 10.0),
                        previousCompanies = gson.toJson(getPreviousCompanies(stock.sector)),
                        education = getEducation(if (stock.sector == "Mining") "Geology" else "Engineering"),
                        specialization = getSpecialization(stock.sector, "Operations"),
                        socialLinkedIn = null,
                        socialTwitter = null
                    )
                    else -> Executive(
                        stockId = stock.id,
                        name = getExecutiveName(),
                        title = "VP Corporate Development",
                        yearsAtCompany = Random.nextDouble(1.0, 8.0),
                        previousCompanies = gson.toJson(getPreviousCompanies(stock.sector)),
                        education = getEducation("Business"),
                        specialization = "Mergers & Acquisitions, Strategic Planning",
                        socialLinkedIn = null,
                        socialTwitter = null
                    )
                }
                executives.add(executive)
            }
        }

        executiveDao.insertAll(executives)
    }

    private fun getExecutiveName(): String {
        val firstNames = listOf(
            "Michael", "Sarah", "David", "Jennifer", "Robert", "Emily", "James", "Lisa",
            "John", "Patricia", "William", "Linda", "Richard", "Barbara", "Thomas", "Susan",
            "Charles", "Jessica", "Daniel", "Karen", "Matthew", "Nancy", "Christopher", "Margaret"
        )
        val lastNames = listOf(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Hernandez", "Lopez", "Wilson", "Anderson", "Thomas", "Taylor",
            "Moore", "Jackson", "Martin", "Lee", "Thompson", "White", "Harris", "Clark"
        )
        return "${firstNames.random()} ${lastNames.random()}"
    }

    private fun getPreviousCompanies(sector: String): List<String> {
        val miningCompanies = listOf("Barrick Gold", "Newmont Corporation", "Kinross Gold", "Agnico Eagle", "Teck Resources")
        val energyCompanies = listOf("Suncor Energy", "Canadian Natural Resources", "Cenovus Energy", "Imperial Oil", "Enbridge")
        val techCompanies = listOf("Shopify", "OpenText", "CGI Group", "Constellation Software", "BlackBerry")
        val generalCompanies = listOf("Deloitte", "PwC", "KPMG", "EY", "McKinsey & Company")

        val companies = when (sector) {
            "Mining" -> miningCompanies + generalCompanies
            "Energy" -> energyCompanies + generalCompanies
            "Technology" -> techCompanies + generalCompanies
            else -> generalCompanies
        }

        return companies.shuffled().take((1..3).random())
    }

    private fun getEducation(specialization: String? = null): String {
        val degrees = when (specialization) {
            "Finance" -> listOf("MBA, Finance", "CPA, Accounting", "B.Comm, Finance", "M.Sc. Finance")
            "Geology" -> listOf("M.Sc. Geology", "B.Sc. Geological Engineering", "Ph.D. Geology", "B.Sc. Earth Sciences")
            "Engineering" -> listOf("B.Sc. Engineering", "M.Eng. Computer Science", "B.Sc. Electrical Engineering", "M.Sc. Mechanical Engineering")
            "Business" -> listOf("MBA", "B.Comm", "B.A. Economics", "M.Sc. Management")
            else -> listOf("MBA", "B.Comm", "B.Sc.", "M.Sc.", "B.A.", "CPA")
        }
        val universities = listOf("University of Toronto", "UBC", "McGill University", "Western University", "Queen's University", "University of Alberta")
        return "${degrees.random()} - ${universities.random()}"
    }

    private fun getSpecialization(sector: String, role: String): String {
        return when (sector) {
            "Mining" -> when (role) {
                "CEO" -> "Mining Operations, Corporate Strategy, Capital Markets"
                "CFO" -> "Financial Planning, Capital Raising, M&A"
                "Operations" -> "Exploration, Mine Development, Geology"
                else -> "Business Development, Investor Relations"
            }
            "Energy" -> when (role) {
                "CEO" -> "Oil & Gas Operations, Energy Markets, Sustainability"
                "CFO" -> "Energy Finance, Risk Management, Capital Allocation"
                "Operations" -> "Production Optimization, Reservoir Engineering"
                else -> "Strategic Planning, Corporate Development"
            }
            "Technology" -> when (role) {
                "CEO" -> "Technology Strategy, Product Innovation, SaaS"
                "CFO" -> "SaaS Metrics, Venture Capital, Financial Strategy"
                "Operations" -> "Software Development, Cloud Infrastructure, AI/ML"
                else -> "Product Management, Growth Strategy"
            }
            else -> when (role) {
                "CEO" -> "Corporate Strategy, Leadership, Operations"
                "CFO" -> "Financial Management, Capital Markets, Reporting"
                "Operations" -> "Operations Management, Process Optimization"
                else -> "Business Development, Strategic Planning"
            }
        }
    }

    /**
     * Checks if seed data has already been populated.
     */
    suspend fun isSeedComplete(): Boolean {
        return context.dataStore.data
            .map { preferences ->
                preferences[SEED_COMPLETE_KEY] ?: false
            }
            .first()
    }

    /**
     * Marks seed data as complete in DataStore preferences.
     */
    suspend fun markSeedComplete() {
        context.dataStore.edit { preferences ->
            preferences[SEED_COMPLETE_KEY] = true
        }
    }
}
