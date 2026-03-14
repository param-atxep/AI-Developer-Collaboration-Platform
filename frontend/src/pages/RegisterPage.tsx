import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Eye,
  EyeOff,
  Leaf,
  Mail,
  Lock,
  User as UserIcon,
  Building,
  Phone,
  MapPin,
  ChevronRight,
  ChevronLeft,
  Check,
} from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import type { UserRole, GeoLocation } from '@/types';
import LoadingSpinner from '@/components/common/LoadingSpinner';

type Step = 1 | 2 | 3;

const roles: { value: UserRole; label: string; description: string }[] = [
  {
    value: 'RESTAURANT',
    label: 'Restaurant / Business',
    description: 'I have surplus food to donate and want to reduce waste',
  },
  {
    value: 'NGO',
    label: 'NGO / Charity',
    description: 'I collect and distribute food to communities in need',
  },
  {
    value: 'CITIZEN',
    label: 'Individual / Volunteer',
    description: 'I want to claim food for personal use or volunteer for pickups',
  },
];

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register, isAuthenticated, loading, error, clearError } = useAuth();

  const [step, setStep] = useState<Step>(1);
  const [showPassword, setShowPassword] = useState(false);

  // Step 1
  const [email, setEmail] = useState('');
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  // Step 2
  const [role, setRole] = useState<UserRole>('CITIZEN');
  const [phone, setPhone] = useState('');
  const [organization, setOrganization] = useState('');

  // Step 3
  const [location, setLocation] = useState<GeoLocation>({
    latitude: 0,
    longitude: 0,
    address: '',
    city: '',
    state: '',
    zipCode: '',
  });

  const [validationError, setValidationError] = useState('');

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  useEffect(() => {
    return () => clearError();
  }, [clearError]);

  const validateStep = (currentStep: Step): boolean => {
    setValidationError('');

    if (currentStep === 1) {
      if (!email || !name || !password || !confirmPassword) {
        setValidationError('Please fill in all fields');
        return false;
      }
      if (password.length < 8) {
        setValidationError('Password must be at least 8 characters');
        return false;
      }
      if (password !== confirmPassword) {
        setValidationError('Passwords do not match');
        return false;
      }
    }

    return true;
  };

  const nextStep = () => {
    if (validateStep(step)) {
      setStep((step + 1) as Step);
    }
  };

  const prevStep = () => {
    setValidationError('');
    setStep((step - 1) as Step);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const result = await register({
      email,
      password,
      name,
      role,
      phone: phone || undefined,
      organization: organization || undefined,
      location: location.address ? location : undefined,
    });
    if (result.meta.requestStatus === 'fulfilled') {
      navigate('/dashboard', { replace: true });
    }
  };

  const handleGetLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLocation((prev) => ({
            ...prev,
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
          }));
        },
        (err) => {
          console.error('Geolocation error:', err);
          setValidationError('Unable to get your location. Please enter it manually.');
        },
      );
    }
  };

  const stepIndicator = (s: number) => (
    <div className="flex items-center gap-2">
      <div
        className={`flex h-8 w-8 items-center justify-center rounded-full text-sm font-semibold transition-colors ${
          step > s
            ? 'bg-primary-500 text-white'
            : step === s
              ? 'bg-primary-500 text-white'
              : 'bg-gray-200 text-gray-500 dark:bg-gray-700 dark:text-gray-400'
        }`}
      >
        {step > s ? <Check className="h-4 w-4" /> : s}
      </div>
    </div>
  );

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center px-4 py-12 bg-gray-50 dark:bg-gray-950">
      <div className="w-full max-w-lg">
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-2 mb-4">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary-500 text-white">
              <Leaf className="h-5 w-5" />
            </div>
          </div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Create your account</h1>
          <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
            Already have an account?{' '}
            <Link to="/login" className="text-primary-600 hover:text-primary-500 font-medium">
              Sign in
            </Link>
          </p>
        </div>

        {/* Step Indicator */}
        <div className="flex items-center justify-center gap-4 mb-8">
          {stepIndicator(1)}
          <div className={`h-0.5 w-12 ${step > 1 ? 'bg-primary-500' : 'bg-gray-200 dark:bg-gray-700'}`} />
          {stepIndicator(2)}
          <div className={`h-0.5 w-12 ${step > 2 ? 'bg-primary-500' : 'bg-gray-200 dark:bg-gray-700'}`} />
          {stepIndicator(3)}
        </div>

        <div className="card p-8">
          {(error || validationError) && (
            <div className="mb-6 p-4 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800">
              <p className="text-sm text-red-700 dark:text-red-400">{error || validationError}</p>
            </div>
          )}

          <form onSubmit={handleSubmit}>
            {/* Step 1: Credentials */}
            {step === 1 && (
              <div className="space-y-4 animate-fade-in">
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                  Account Information
                </h2>

                <div>
                  <label htmlFor="name" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                    Full Name
                  </label>
                  <div className="relative">
                    <UserIcon className="absolute left-3.5 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <input
                      id="name"
                      type="text"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      placeholder="John Doe"
                      className="input-field pl-11"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="reg-email" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                    Email Address
                  </label>
                  <div className="relative">
                    <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <input
                      id="reg-email"
                      type="email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      placeholder="you@example.com"
                      className="input-field pl-11"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="reg-password" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                    Password
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <input
                      id="reg-password"
                      type={showPassword ? 'text' : 'password'}
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="Min. 8 characters"
                      className="input-field pl-11 pr-11"
                      required
                      minLength={8}
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                      {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                    </button>
                  </div>
                </div>

                <div>
                  <label htmlFor="confirm-password" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                    Confirm Password
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <input
                      id="confirm-password"
                      type="password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="Confirm your password"
                      className="input-field pl-11"
                      required
                    />
                  </div>
                </div>

                <button type="button" onClick={nextStep} className="btn-primary w-full py-3 mt-4 gap-2">
                  Continue <ChevronRight className="h-4 w-4" />
                </button>
              </div>
            )}

            {/* Step 2: Role & Profile */}
            {step === 2 && (
              <div className="space-y-4 animate-fade-in">
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                  Choose Your Role
                </h2>

                <div className="space-y-3">
                  {roles.map((r) => (
                    <label
                      key={r.value}
                      className={`block p-4 rounded-xl border-2 cursor-pointer transition-all ${
                        role === r.value
                          ? 'border-primary-500 bg-primary-50 dark:bg-primary-900/20 dark:border-primary-500'
                          : 'border-gray-200 hover:border-gray-300 dark:border-gray-700 dark:hover:border-gray-600'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <input
                          type="radio"
                          name="role"
                          value={r.value}
                          checked={role === r.value}
                          onChange={() => setRole(r.value)}
                          className="h-4 w-4 text-primary-500 focus:ring-primary-500"
                        />
                        <div>
                          <p className="font-medium text-gray-900 dark:text-white">{r.label}</p>
                          <p className="text-sm text-gray-500 dark:text-gray-400">{r.description}</p>
                        </div>
                      </div>
                    </label>
                  ))}
                </div>

                {(role === 'RESTAURANT' || role === 'NGO') && (
                  <div>
                    <label htmlFor="organization" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                      Organization Name
                    </label>
                    <div className="relative">
                      <Building className="absolute left-3.5 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                      <input
                        id="organization"
                        type="text"
                        value={organization}
                        onChange={(e) => setOrganization(e.target.value)}
                        placeholder="Your organization name"
                        className="input-field pl-11"
                      />
                    </div>
                  </div>
                )}

                <div>
                  <label htmlFor="phone" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                    Phone Number (optional)
                  </label>
                  <div className="relative">
                    <Phone className="absolute left-3.5 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <input
                      id="phone"
                      type="tel"
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      placeholder="+1 (555) 000-0000"
                      className="input-field pl-11"
                    />
                  </div>
                </div>

                <div className="flex gap-3 mt-4">
                  <button type="button" onClick={prevStep} className="btn-outline flex-1 py-3 gap-2">
                    <ChevronLeft className="h-4 w-4" /> Back
                  </button>
                  <button type="button" onClick={nextStep} className="btn-primary flex-1 py-3 gap-2">
                    Continue <ChevronRight className="h-4 w-4" />
                  </button>
                </div>
              </div>
            )}

            {/* Step 3: Location */}
            {step === 3 && (
              <div className="space-y-4 animate-fade-in">
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                  Your Location
                </h2>
                <p className="text-sm text-gray-500 dark:text-gray-400 mb-4">
                  Help us show you nearby food listings and opportunities.
                </p>

                <button
                  type="button"
                  onClick={handleGetLocation}
                  className="btn-secondary w-full py-3 gap-2 mb-4"
                >
                  <MapPin className="h-4 w-4" />
                  Use My Current Location
                </button>

                {location.latitude !== 0 && (
                  <div className="mb-4 p-3 rounded-lg bg-primary-50 dark:bg-primary-900/20 text-sm text-primary-700 dark:text-primary-400">
                    Location detected: {location.latitude.toFixed(4)}, {location.longitude.toFixed(4)}
                  </div>
                )}

                <div className="grid grid-cols-1 gap-4">
                  <div>
                    <label htmlFor="address" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                      Street Address
                    </label>
                    <input
                      id="address"
                      type="text"
                      value={location.address}
                      onChange={(e) => setLocation({ ...location, address: e.target.value })}
                      placeholder="123 Main Street"
                      className="input-field"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label htmlFor="city" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                        City
                      </label>
                      <input
                        id="city"
                        type="text"
                        value={location.city}
                        onChange={(e) => setLocation({ ...location, city: e.target.value })}
                        placeholder="New York"
                        className="input-field"
                      />
                    </div>
                    <div>
                      <label htmlFor="state" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                        State
                      </label>
                      <input
                        id="state"
                        type="text"
                        value={location.state}
                        onChange={(e) => setLocation({ ...location, state: e.target.value })}
                        placeholder="NY"
                        className="input-field"
                      />
                    </div>
                  </div>

                  <div className="w-1/2">
                    <label htmlFor="zipCode" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                      ZIP Code
                    </label>
                    <input
                      id="zipCode"
                      type="text"
                      value={location.zipCode}
                      onChange={(e) => setLocation({ ...location, zipCode: e.target.value })}
                      placeholder="10001"
                      className="input-field"
                    />
                  </div>
                </div>

                {/* Map placeholder */}
                <div className="h-48 rounded-xl bg-gray-100 dark:bg-gray-800 border-2 border-dashed border-gray-300 dark:border-gray-600 flex items-center justify-center">
                  <div className="text-center">
                    <MapPin className="h-8 w-8 text-gray-400 mx-auto mb-2" />
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      Map picker will show here
                    </p>
                  </div>
                </div>

                <div className="flex gap-3 mt-4">
                  <button type="button" onClick={prevStep} className="btn-outline flex-1 py-3 gap-2">
                    <ChevronLeft className="h-4 w-4" /> Back
                  </button>
                  <button type="submit" disabled={loading} className="btn-primary flex-1 py-3 gap-2">
                    {loading ? <LoadingSpinner size="sm" /> : 'Create Account'}
                  </button>
                </div>
              </div>
            )}
          </form>
        </div>

        <p className="mt-6 text-center text-xs text-gray-400 dark:text-gray-500">
          By registering, you agree to our{' '}
          <a href="#" className="text-primary-600 hover:text-primary-500">Terms of Service</a>
          {' '}and{' '}
          <a href="#" className="text-primary-600 hover:text-primary-500">Privacy Policy</a>.
        </p>
      </div>
    </div>
  );
}
