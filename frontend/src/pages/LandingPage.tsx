import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowRight,
  Leaf,
  Truck,
  Users,
  BarChart3,
  ChevronRight,
  Utensils,
  MapPin,
  Heart,
} from 'lucide-react';

function AnimatedCounter({ end, duration = 2000, suffix = '' }: { end: number; duration?: number; suffix?: string }) {
  const [count, setCount] = useState(0);
  const ref = useRef<HTMLDivElement>(null);
  const started = useRef(false);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && !started.current) {
          started.current = true;
          const startTime = Date.now();
          const animate = () => {
            const elapsed = Date.now() - startTime;
            const progress = Math.min(elapsed / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3);
            setCount(Math.floor(eased * end));
            if (progress < 1) {
              requestAnimationFrame(animate);
            }
          };
          requestAnimationFrame(animate);
        }
      },
      { threshold: 0.3 },
    );

    if (ref.current) observer.observe(ref.current);
    return () => observer.disconnect();
  }, [end, duration]);

  return (
    <div ref={ref}>
      <span className="text-4xl md:text-5xl font-bold text-gradient">
        {count.toLocaleString()}{suffix}
      </span>
    </div>
  );
}

export default function LandingPage() {
  const steps = [
    {
      icon: Utensils,
      title: 'List Surplus Food',
      description:
        'Restaurants and businesses list their surplus food items with details about quantity, expiry, and pickup times.',
      color: 'bg-primary-100 text-primary-600 dark:bg-primary-900/30 dark:text-primary-400',
    },
    {
      icon: MapPin,
      title: 'AI-Powered Matching',
      description:
        'Our AI algorithm matches available food with nearby NGOs and community members based on proximity, needs, and dietary preferences.',
      color: 'bg-secondary-100 text-secondary-600 dark:bg-secondary-900/30 dark:text-secondary-400',
    },
    {
      icon: Truck,
      title: 'Coordinate Pickup',
      description:
        'Schedule and track pickups in real-time. Volunteers can claim deliveries and optimize routes with our smart logistics.',
      color: 'bg-accent-100 text-accent-600 dark:bg-accent-900/30 dark:text-accent-400',
    },
  ];

  const impactStats = [
    { value: 50000, suffix: '+', label: 'Meals Redistributed' },
    { value: 25, suffix: 'T', label: 'CO2 Emissions Saved' },
    { value: 500, suffix: '+', label: 'Partner Restaurants' },
    { value: 10000, suffix: '+', label: 'Community Members' },
  ];

  return (
    <div className="bg-white dark:bg-gray-950 -mt-16">
      {/* Hero Section */}
      <section className="relative overflow-hidden pt-16">
        <div className="absolute inset-0 bg-gradient-to-br from-primary-50 via-white to-secondary-50 dark:from-gray-950 dark:via-gray-900 dark:to-gray-950" />
        <div className="absolute top-20 left-10 w-72 h-72 bg-primary-200/30 rounded-full blur-3xl dark:bg-primary-500/10" />
        <div className="absolute bottom-20 right-10 w-96 h-96 bg-secondary-200/20 rounded-full blur-3xl dark:bg-secondary-500/10" />

        <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-24 md:py-36">
          <div className="max-w-4xl mx-auto text-center">
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary-100 dark:bg-primary-900/30 text-primary-700 dark:text-primary-400 text-sm font-medium mb-8">
              <Leaf className="h-4 w-4" />
              AI-Powered Food Waste Solution
            </div>
            <h1 className="text-5xl md:text-7xl font-extrabold text-gray-900 dark:text-white leading-tight tracking-tight">
              Reduce Food Waste,{' '}
              <span className="text-gradient">Feed Communities</span>
            </h1>
            <p className="mt-6 text-lg md:text-xl text-gray-600 dark:text-gray-400 max-w-2xl mx-auto leading-relaxed">
              Connect surplus food from restaurants with people who need it most.
              Our AI-powered platform makes food redistribution efficient,
              transparent, and impactful.
            </p>
            <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
              <Link
                to="/register"
                className="btn-primary text-base px-8 py-4 gap-2 shadow-lg shadow-primary-500/25 hover:shadow-primary-500/40 transition-all"
              >
                Get Started Free
                <ArrowRight className="h-5 w-5" />
              </Link>
              <Link
                to="/food"
                className="btn-outline text-base px-8 py-4 gap-2"
              >
                Browse Available Food
                <ChevronRight className="h-5 w-5" />
              </Link>
            </div>

            {/* Trust indicators */}
            <div className="mt-16 flex items-center justify-center gap-8 text-sm text-gray-400 dark:text-gray-500">
              <div className="flex items-center gap-2">
                <Heart className="h-4 w-4 text-red-400" />
                Trusted by 500+ restaurants
              </div>
              <div className="hidden sm:flex items-center gap-2">
                <Users className="h-4 w-4 text-secondary-400" />
                10,000+ community members
              </div>
              <div className="hidden md:flex items-center gap-2">
                <BarChart3 className="h-4 w-4 text-primary-400" />
                50,000+ meals saved
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-24 bg-gray-50 dark:bg-gray-900/50">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white">
              How It Works
            </h2>
            <p className="mt-4 text-lg text-gray-600 dark:text-gray-400 max-w-2xl mx-auto">
              Three simple steps to reduce food waste and make a difference in your community.
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            {steps.map((step, index) => (
              <div
                key={step.title}
                className="relative card p-8 text-center group hover:-translate-y-1 transition-transform duration-300"
              >
                <div className="absolute top-4 right-4 text-6xl font-bold text-gray-100 dark:text-gray-800 select-none">
                  {index + 1}
                </div>
                <div
                  className={`inline-flex h-16 w-16 items-center justify-center rounded-2xl ${step.color} mb-6`}
                >
                  <step.icon className="h-8 w-8" />
                </div>
                <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-3">
                  {step.title}
                </h3>
                <p className="text-gray-600 dark:text-gray-400 leading-relaxed">
                  {step.description}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Impact Stats */}
      <section className="py-24 bg-white dark:bg-gray-950">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white">
              Our Impact
            </h2>
            <p className="mt-4 text-lg text-gray-600 dark:text-gray-400">
              Together, we are making a real difference in reducing food waste.
            </p>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
            {impactStats.map((stat) => (
              <div key={stat.label} className="text-center">
                <AnimatedCounter end={stat.value} suffix={stat.suffix} />
                <p className="mt-2 text-sm md:text-base text-gray-600 dark:text-gray-400 font-medium">
                  {stat.label}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-24 bg-gradient-to-r from-primary-500 to-primary-600 dark:from-primary-600 dark:to-primary-700">
        <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl md:text-4xl font-bold text-white">
            Ready to Make a Difference?
          </h2>
          <p className="mt-4 text-lg text-primary-100 max-w-2xl mx-auto">
            Join our growing community of restaurants, NGOs, and citizens working together
            to eliminate food waste and feed those in need.
          </p>
          <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link
              to="/register"
              className="inline-flex items-center gap-2 px-8 py-4 text-base font-semibold text-primary-600 bg-white rounded-lg hover:bg-gray-50 transition-colors shadow-lg"
            >
              Join as Restaurant
              <Utensils className="h-5 w-5" />
            </Link>
            <Link
              to="/register"
              className="inline-flex items-center gap-2 px-8 py-4 text-base font-semibold text-white border-2 border-white/30 rounded-lg hover:bg-white/10 transition-colors"
            >
              Join as Volunteer
              <Heart className="h-5 w-5" />
            </Link>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 dark:bg-black py-12">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row items-center justify-between gap-6">
            <div className="flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary-500 text-white">
                <Leaf className="h-4 w-4" />
              </div>
              <span className="text-lg font-bold text-white">
                Food<span className="text-primary-400">Bridge</span>
              </span>
            </div>
            <div className="flex items-center gap-6 text-sm text-gray-400">
              <Link to="/food" className="hover:text-white transition-colors">
                Browse Food
              </Link>
              <Link to="/map" className="hover:text-white transition-colors">
                Map
              </Link>
              <Link to="/analytics" className="hover:text-white transition-colors">
                Impact
              </Link>
              <a href="#" className="hover:text-white transition-colors">
                Privacy
              </a>
              <a href="#" className="hover:text-white transition-colors">
                Terms
              </a>
            </div>
            <p className="text-sm text-gray-500">
              &copy; 2026 FoodBridge. All rights reserved.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
}
