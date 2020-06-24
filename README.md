# react-native-chargebee

Package for chargebee checkout

## Installation

```sh
npm install react-native-chargebee
```

## Usage

```js
import CheckoutCart from "react-native-chargebee";

// ...

<CheckoutCart
  onSuccess={(hostedPageId: string) => handleSuccessfulPurchase(hostedPageId)}
  step={(stepName: string) => handleStep(stepName)}
  site='site_name'
  planName={planName}
  couponIds={couponIds}
  addons={addons}
  customer={{ email: 'viveknow@chargebee.com' }}
  billingAddress={billingAddress}
/>
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
