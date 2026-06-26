export type CriterionKey = 'weather' | 'cost' | 'distance' | 'festivities'

export type ProfileKey = 'economico' | 'clima-perfeito' | 'aventura' | 'cultural' | 'equilibrado'

export type Region = 'Africa' | 'Americas' | 'Asia' | 'Europe' | 'Oceania'

export interface ApiLimits {
  recommendationWindowDays: number
  bestWindowsPeriodDays: number
  maximumResults: number
  maximumExplicitCandidates: number
  maximumRegionCandidates: number
  maximumTravelers: number
}

export interface RegionOption {
  key: Region
  label: string
}

export interface CriterionOption {
  key: CriterionKey
  label: string
  icon: string
  defaultWeight: number
}

export interface ProfileOption {
  key: ProfileKey
  label: string
  weights: Record<CriterionKey, number>
}

export interface CityOption {
  name: string
  latitude: number
  longitude: number
  primary: boolean
}

export interface CountryOption {
  code: string
  name: string
  flagEmoji: string
  region: Region
  subregion: string
  defaultCity: string
  cities: CityOption[]
}

export interface DataSourceInfo {
  key: string
  label: string
  mode: 'STATIC' | 'LIVE' | 'LIVE_AND_HISTORICAL' | 'LIVE_CACHED'
  purpose: string
}

export interface ApiMetaResponse {
  apiVersion: string
  catalogVersion: string
  limits: ApiLimits
  regions: RegionOption[]
  criteria: CriterionOption[]
  profiles: ProfileOption[]
  countries: CountryOption[]
  dataSources: DataSourceInfo[]
  capabilities: Record<string, boolean>
}

export interface OriginInput {
  countryCode?: string
  subdivisionCode?: string
  latitude?: number
  longitude?: number
  city?: string
}

export interface RecommendationSearchRequest {
  from: string
  to: string
  countries?: string[]
  region?: Region
  limit?: number
  profile?: ProfileKey
  weights?: Partial<Record<CriterionKey, number>>
  exclude?: string[]
  origin?: OriginInput
  travelers?: number
  maxGroundBudgetBrl?: number
}

export interface OriginReference {
  countryCode: string
  subdivisionCode: string | null
  latitude: number
  longitude: number
  cityName: string | null
}

export interface LongWeekend {
  start: string
  end: string
  totalDays: number
  bridgeDaysUsed: number
  holidayName: string
}

export interface WindowAssessment {
  score: number
  totalDays: number
  freeDays: number
  requiredLeaveDays: number
  longWeekends: LongWeekend[]
  explanation: string
}

export interface DataQuality {
  coverage: number
  confidenceScore: number
  availableCriteria: number
  totalCriteria: number
  missingCriteria: CriterionKey[]
}

export interface ScoredCriterion {
  criterion: CriterionKey
  label: string
  icon: string
  available: boolean
  score: number
  weight: number
  contribution: number
  justification: string
}

export interface Exchange {
  currency: string
  valueInReais: number
}

export interface DestinationProfile {
  flagEmoji: string | null
  population: number | null
  populationYear: string | null
  description: string | null
  extract: string | null
  imageUrl: string | null
  wikipediaUrl: string | null
}

export interface DestinationCity {
  countryCode: string
  name: string
  latitude: number
  longitude: number
  utcOffsets: number[]
  primary: boolean
}

export interface TravelEffort {
  distanceKm: number
  estimatedTravelHoursMin: number
  estimatedTravelHoursMax: number
  originUtcOffset: number | null
  destinationUtcOffset: number | null
  timeZoneDifferenceHours: number | null
  classification: 'short' | 'medium' | 'long'
  estimated: boolean
}

export interface GroundCostEstimate {
  currency: string
  estimatedDailyPerPerson: number
  estimatedTotal: number
  travelers: number
  days: number
  relativePriceLevel: number
  destinationDataYear: string
  originDataYear: string
  confidence: 'high' | 'medium' | 'low'
  assumption: string
}

export interface TripFeasibility {
  destination: DestinationCity
  travelEffort: TravelEffort
  groundCost: GroundCostEstimate | null
  notIncluded: string[]
}

export interface TravelRecommendation {
  countryCode: string
  countryName: string
  destinationScore: number
  windowScore: number
  tripScore: number
  dataQuality: DataQuality
  breakdown: ScoredCriterion[]
  highlights: string[]
  tradeoffs: string[]
  summary: string
  exchangeToBrl: Exchange | null
  profile: DestinationProfile
  feasibility: TripFeasibility | null
}

export interface SkippedCandidate {
  countryCode: string
  reason: string
}

export interface RecommendationResponse {
  from: string
  to: string
  generatedAt: string
  origin: OriginReference
  profile: ProfileKey | null
  weights: Record<CriterionKey, number>
  window: WindowAssessment
  recommendations: TravelRecommendation[]
  skipped: SkippedCandidate[]
}

export interface WindowSuggestion {
  start: string
  end: string
  totalDays: number
  bridgeDaysUsed: number
  requiredLeaveDays: number
  label: string
  timingScore: number
  topDestinations: TravelRecommendation[]
}

export interface BestWindowsResponse {
  from: string
  to: string
  profile: ProfileKey | null
  windows: WindowSuggestion[]
}

export interface Country {
  name: string
  localizedName: string | null
  isoCode: string
  region: Region
  subregion: string
  capitals: string[]
  languages: string[]
  currencies: string[]
  timezones: string[]
  latitude: number | null
  longitude: number | null
  independent: boolean
  unMember: boolean
  status: string | null
}

export interface Holiday {
  date: string
  name: string
  localName: string
  types: string[]
  global: boolean
  subdivisions: string[]
}

export interface TravelOverview {
  country: Country
  upcomingHolidays: Holiday[]
  exchangeToBrl: Exchange | null
  profile: DestinationProfile
}

export interface ApiErrorBody {
  timestamp: string
  status: number
  error: string
  message: string
}

export interface BestWindowsQuery {
  from: string
  to: string
  minDays?: number
  topWindows?: number
  countries?: string[]
  region?: Region
  destinationsPerWindow?: number
  profile?: ProfileKey
  weights?: Partial<Record<CriterionKey, number>>
  exclude?: string[]
  originCountry?: string
  originSubdivision?: string
  originLatitude?: number
  originLongitude?: number
  originCity?: string
  travelers?: number
  maxGroundBudget?: number
}
